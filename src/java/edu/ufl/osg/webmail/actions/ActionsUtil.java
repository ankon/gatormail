/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002, 2003 William A. McArthur, Jr.
 * Copyright (C) 2003-2005 The Open Systems Group / University of Florida
 * Copyright (C) 2004 Allison Moore
 *
 * GatorMail is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * GatorMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GatorMail; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ufl.osg.webmail.actions;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.Quota;
import com.sun.mail.iap.CommandFailedException;
import com.opensymphony.oscache.web.ServletCacheAdministrator;
import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.NeedsRefreshException;
import edu.ufl.osg.webmail.ComposeSavedException;
import edu.ufl.osg.webmail.Constants;
import edu.ufl.osg.webmail.InvalidSessionException;
import edu.ufl.osg.webmail.SessionExpiredException;
import edu.ufl.osg.webmail.User;
import edu.ufl.osg.webmail.beans.QuotaBean;
import edu.ufl.osg.webmail.forms.ComposeForm;
import edu.ufl.osg.webmail.util.Util;
import edu.ufl.osg.webmail.util.Util.DateSort;
import edu.ufl.osg.webmail.util.Util.DateSortU;
import edu.ufl.osg.webmail.util.Util.FromSort;
import edu.ufl.osg.webmail.util.Util.FromSortU;
import edu.ufl.osg.webmail.util.Util.SizeSort;
import edu.ufl.osg.webmail.util.Util.SizeSortU;
import edu.ufl.osg.webmail.util.Util.SubjectSort;
import edu.ufl.osg.webmail.util.Util.SubjectSortU;
import edu.ufl.osg.webmail.util.Util.ToSort;
import edu.ufl.osg.webmail.util.Util.ToSortU;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Package local util class.
 *
 * @author sandymac
 * @author drakee
 * @since Feb 26, 2003 2:44:15 PM
 * @version $Revision: 1.22 $
 */
final class ActionsUtil {
    private static final Logger logger = Logger.getLogger(ActionsUtil.class.getName());

    private static final String MAIL_STORE_GROUP = "mailStore";
    private static final String[] MAIL_STORE_CACHE_GROUP = new String[] {MAIL_STORE_GROUP};
    private static final Folder[] NO_FOLDERS = new Folder[0];

    /**
     * Checks the users session and throws an exception if a problem is found.
     *
     * @param  request                 The http request.
     * @throws SessionExpiredException If the user's session has expired.
     * @throws InvalidSessionException If the user's session is out of wack for
     *                                 some reason.
     */
    public static void checkSession(final HttpServletRequest request) throws InvalidSessionException, NoSuchProviderException {
        final HttpSession session = request.getSession();

        // Cheeze hack to let us track how long a request took.
        if (request.getAttribute("requestStartTime") == null) {
            request.setAttribute("requestStartTime", new Long(System.currentTimeMillis()));
        }

        // check if the user was working on or trying to send a message
        // when his/her session timed out
        checkComposeSaved(request, session);

        if (session.isNew()) {
            //logger.debug("New session: " + session.hashCode());
            throw new SessionExpiredException("New session created");
        }

        if (Boolean.TRUE.equals(session.getAttribute(Constants.LOGGING_IN))) {
            throw new InvalidSessionException("User trying to login in concurrently.");
        }

        final User user = (User)session.getAttribute(Constants.USER_KEY);

        if (user == null) {
            throw new InvalidSessionException("Missing User object");
        }

        final String glid = (String)request.getAttribute("UF_GatorLinkID");
        if (glid != null && !user.getUsername().equals(glid)) {
            throw new InvalidSessionException("GLID and HttpSession mismatch.");
        }

        final Session mailSession = user.getMailSession();
        if (mailSession == null) { // should never happen
            throw new InvalidSessionException("Missing JavaMail Session object");
        }

        final Store mailStore = user.getMailStore();
        if (mailStore == null) { // should never happen
            throw new InvalidSessionException("Missing JavaMail Store object");
        }

        // Update activity
        try {
            user.updateActivity(request);
        } catch (Throwable t) {
            logger.error("Swallowed problem updating the user's activity.", t);
        }
    }

    // check if the user was working on or trying to send a message
    // when his/her session timed out
    private static void checkComposeSaved(final HttpServletRequest request, final HttpSession session) throws ComposeSavedException {
        // check if user is logged in
        if (Util.getUser(session) != null)
            return; // user is logged in, so the session is valid. no point continuing.

        final ComposeForm compForm = (ComposeForm)request.getAttribute("composeForm");
        // composeForm would be in request when user submits anything on the compose page,
        // even if the session is no longer valid.
        if (compForm != null && session.getAttribute(Constants.SAVED_COMPOSE_FORM) == null) {
            logger.debug("user has an active composeForm in request");
            final String body = compForm.getBody();
            // If body has any content, then we save the compose form.
            // Otherwise the user either had just clicked "compose", "reply", or
            // "forward" when the session timed out, or at worst, s/he doesn't have
            // enough of a message to be worth saving.
            if (body != null && !body.trim().equals("")) {
                logger.debug("... let's save the message");
                session.setAttribute(Constants.SAVED_COMPOSE_FORM, compForm);
                throw new ComposeSavedException("New session created. Saved composed message.");
            }
        }
    }

    /**
     * Fetch the Folder specified from a FolderForm instance.
     *
     * Returned folder is always open.
     *
     * @param form                the struts ActionForm
     * @param request             the submitted HTTP request
     */
    protected static Folder fetchFolder(final ActionForm form, final HttpServletRequest request) throws IllegalAccessException, MessagingException, InvocationTargetException, NoSuchMethodException, FolderNotFoundException {
        final String folderName = (String)PropertyUtils.getSimpleProperty(form, "folder");

        Folder folder = null;
        try {
            folder = Util.getFolder(request.getSession(), folderName);
        } catch (FolderNotFoundException fnfe) {
            logger.error("folder not found: " + folderName);
            throw new FolderNotFoundException(folder, fnfe.toString());
        }

        return folder;
    }

    /**
     * Fetch a message from a Folder.
     *
     * @param form                the struts ActionForm
     * @param folder              the JavaMail Folder containting the message
     */
    protected static Message fetchMessage(final ActionForm form, final Folder folder) throws IllegalAccessException, MessagingException, InvocationTargetException, NoSuchMethodException {
        final Integer messageNumber = (Integer)PropertyUtils.getSimpleProperty(form, "messageNumber");
        final Long uid = (Long)PropertyUtils.getSimpleProperty(form, "uid");
        final Message message;

        // Find the message
        if (uid != null) {
            // UID to get message
            final IMAPFolder imapFolder = (IMAPFolder)folder;
            message = imapFolder.getMessageByUID(uid.longValue());
        } else {
            // messageNumber to get message
            message = folder.getMessage(messageNumber.intValue());
        }

        return message;
    }

    /**
     * Fetch messages, which have been selected by checkbox, from a Folder.
     *
     * @param uid                 the array of UIDs
     * @param messageNumber       the array of messageNumbers
     */
    protected static Message[] fetchMessages(final Folder folder, final Long[] uid, final Integer[] messageNumber) throws MessagingException {
        final Message[] messages;
        if (folder instanceof IMAPFolder) {
            final IMAPFolder imapFolder = (IMAPFolder)folder;
            messages = new Message[uid.length];
            for (int i = 0; i < uid.length; i++) {
                messages[i] = imapFolder.getMessageByUID(uid[i].longValue());
            }
        } else {
            messages = new Message[messageNumber.length];
            for (int i = 0; i < messageNumber.length; i++) {
                messages[i] = folder.getMessage(messageNumber[i].intValue());
            }
        }
        return messages;
    }

    private static Iterator getSortedMessageIterator(final Folder folder, final Comparator comparator) throws MessagingException {
        return getSortedMessageIterator(folder, comparator, false);
    }

    private static Iterator getReverseSortedMessageIterator(final Folder folder, final Comparator comparator) throws MessagingException {
        return getSortedMessageIterator(folder, comparator, true);
    }

    private static Iterator getSortedMessageIterator(final Folder folder, final Comparator comparator, final boolean makeReverse) throws MessagingException {
        logger.debug("folder: " + folder + ", comparator: " + comparator);
        final List messageList = new ArrayList(Arrays.asList(folder.getMessages()));

        final ListIterator messageIterator = messageList.listIterator();
        while (messageIterator.hasNext()) {
            final Message m = (Message)messageIterator.next();
            if (m.isExpunged()) {
                messageIterator.remove();
            }
        }

        Collections.sort(messageList, comparator);
        if (makeReverse) {
            Collections.reverse(messageList);
        }

        return messageList.iterator();
    }

    /**
     * Fetch a message from a Folder.
     */
    protected static Message fetchNextMessage(final Message message, final Comparator comparator) throws MessagingException {
        final Folder folder = message.getFolder();
        final Iterator it = getSortedMessageIterator(folder, comparator);
        return getNextMessageInSequence(message, it);
    }

    /**
     * Fetch a message from a Folder.
     */
    protected static Message fetchPrevMessage(final Message message, final Comparator comparator) throws MessagingException {
        final Folder folder = message.getFolder();
        final Iterator it = getReverseSortedMessageIterator(folder, comparator);
        return getNextMessageInSequence(message, it);
    }

    private static Message getNextMessageInSequence(final Message message, final Iterator it) throws MessagingException {
        final Folder folder = message.getFolder();
        // Find the message
        Message nextMessage = null;
        if (folder instanceof IMAPFolder) {
            final IMAPFolder imapFolder = (IMAPFolder)folder;
            final long uid = imapFolder.getUID(message);
            for (; it.hasNext() && nextMessage == null;) {
                final Message m = (Message)it.next();
                if (imapFolder.getUID(m) == uid) {
                    if (it.hasNext())
                        nextMessage = (Message)it.next();
                }
            }
        } else {
            final int messageNumber = message.getMessageNumber();
            for (; it.hasNext() && nextMessage == null;) {
                final Message m = (Message)it.next();
                if (m.getMessageNumber() == messageNumber) {
                    if (it.hasNext())
                        nextMessage = (Message)it.next();
                }
            }
        }
        return nextMessage;
    }

    /**
     * Calls {@link #flushGroupCache(String, javax.servlet.http.HttpSession)} with the mail store group.
     */
    static void flushMailStoreGroupCache(final HttpSession session) {
        flushGroupCache(MAIL_STORE_GROUP, session);
    }

    /**
     * Flush any caches associated with the group.
     * @param group the OSCache group to flush.
     * @param session the user's {@link HttpSession}.
     */
    static void flushGroupCache(final String group, final HttpSession session) {
        final ServletCacheAdministrator admin = ServletCacheAdministrator.getInstance(session.getServletContext());
        final Cache cache = admin.getSessionScopeCache(session);
        cache.flushGroup(group);
    }

    static Set getCachedQuotas(final Folder folder, final HttpSession session) throws MessagingException {
        final ServletCacheAdministrator admin = ServletCacheAdministrator.getInstance(session.getServletContext());
        final Cache cache = admin.getSessionScopeCache(session);
        final String key = "getCachedQuotas#" + folder.getFullName();

        Set content;
        try {
            content = (Set)cache.getFromCache(key, 300);
        } catch (NeedsRefreshException nre) {
            try {
                final String imapQuotaRoots = session.getServletContext().getInitParameter("imap.quota.roots");
                if (imapQuotaRoots == null) {
                    content = getAllQuotas(folder);
                } else {
                    content = getSpecifiedQuotas(imapQuotaRoots, folder);
                }
                cache.putInCache(key, content, MAIL_STORE_CACHE_GROUP);
            } catch (Exception ex) {
                content = (Set)nre.getCacheContent();
                cache.cancelUpdate(key);
            }
        }
        return content;
    }

    private static Set getSpecifiedQuotas(final String imapQuotaRoots, final Folder aFolder) throws MessagingException {
        final String[] quotaRoots = imapQuotaRoots.split(",");
        final List folderList = new ArrayList(quotaRoots.length);
        for (int i=0; i < quotaRoots.length; i++) {
            folderList.add(aFolder.getStore().getFolder(quotaRoots[i]));
        }
        return getQuotasFor(folderList);
    }

    /**
     * Returns a {@link Set} of all known {@link QuotaBean quotas} under a folder.
     * @param folder the root folder to start looking for {@link QuotaBean quotas}.
     * @return a {@link Set} of {@link QuotaBean quotas}.
     * @throws MessagingException when JavaMail pukes.
     */
    static Set getAllQuotas(final Folder folder) throws MessagingException {
        final Set quotaSet = new TreeSet();
        populateAllQuotas(folder, quotaSet);
        return quotaSet;
    }

    static Set getQuotasFor(final List folderList) throws MessagingException {
        final Set quotaSet = new TreeSet();
        final Iterator folderIter = folderList.iterator();
        while (folderIter.hasNext()) {
            final Folder aFolder = (Folder)folderIter.next();
            try {
                addQuotas(aFolder, quotaSet);
            } catch (MessagingException me) {
                logger.error("Problem fetching quota from folder: " + aFolder.getFullName(), me);
            }
        }
        return quotaSet;
    }

    private static void populateAllQuotas(final Folder folder, final Set quotaSet) throws MessagingException {
        final Folder[] folders = folder.list("*");
        final List folderList = Arrays.asList(folders);
        final Iterator folderIter = folderList.iterator();
        while (folderIter.hasNext()) {
            final Folder aFolder = (Folder)folderIter.next();
            try {
                addQuotas(aFolder, quotaSet);
            } catch (MessagingException me) {
                logger.error("Problem fetching quota from folder: " + aFolder.getFullName(), me);
            }
        }
    }

    /**
     * Adds {@link QuotaBean}s to the quota set for the folder.
     * @param folder the quota root
     * @param quotaSet the set of {@link QuotaBean}s
     * @throws MessagingException when JavaMail errors
     */
    private static void addQuotas(final Folder folder, final Set quotaSet) throws MessagingException {
        if (folder instanceof IMAPFolder) {
            final List quotas = Arrays.asList(((IMAPFolder)folder).getQuota());
            final Iterator qIter = quotas.iterator();
            while (qIter.hasNext()) {
                final Quota quota = (Quota)qIter.next();
                final QuotaBean qb = new QuotaBean(quota.quotaRoot);

                final List quotaResources = Arrays.asList(quota.resources);
                final Iterator qrIter = quotaResources.iterator();
                while (qrIter.hasNext()) {
                    final Quota.Resource qr = (Quota.Resource)qrIter.next();
                    final QuotaBean.ResourceBean qbrb = new QuotaBean.ResourceBean(qr.name, qr.usage, qr.limit);
                    qb.addResource(qbrb);
                }
                quotaSet.add(qb);
            }
        }
    }

    /**
     * Get a list of all subscribed folders, under and including the rootFolder.
     *
     * @param  rootFolder          the folder who we want to create a list from
     * @return                     List of FolderBeans
     */
    protected static List getFolderBeanList(final HttpSession session, final Folder rootFolder) throws MessagingException {
        return getFolderBeanList(session, rootFolder, false);
    }

    /**
     * Get a list of all folders, under and including the rootFolder.
     *
     * @param  rootFolder          the folder who we want to create a list from
     * @param  includeUnsubscribed true if we want to include folders that
     *                             aren't subscribed.
     * @return                     List of FolderBeans
     */
    protected static List getFolderBeanList(final HttpSession session, final Folder rootFolder, final boolean includeUnsubscribed) throws MessagingException {
        final List list = new ArrayList();
        setFolderBeanList(session, list, rootFolder, true, includeUnsubscribed);
        return list;
    }

    /**
     * recursively set a list of folders, under and including the rootFolder.
     *
     * @param list                The list to add folders to.
     * @param rootFolder          the folder who we want to create a list from
     * @param includeReserved     true if we want to include reserved folders,
     *                            such as INBOX and INBOX.Trash.
     * @param includeUnsubscribed true if we want to include folders that aren't
     *                            subscribed.
     */
    private static void setFolderBeanList(final HttpSession session, final List list, final Folder rootFolder, final boolean includeReserved, final boolean includeUnsubscribed) throws MessagingException {

        Folder[] folders;
        try {
            // add root folder, if it is allowed
            if (isFolderIncluded(rootFolder, includeReserved, session) && rootFolder.exists()) {
                //list.add(folderToFolderBean(rootFolder));
                list.add(rootFolder);
            }

            try {
                folders = getFolderList(rootFolder, includeUnsubscribed);
            } catch (FolderNotFoundException fnfe) {
                folders = NO_FOLDERS;
            }
        } finally {
            Util.releaseFolder(rootFolder);
        }

        for (int i = 0; i < folders.length; i++) {
            setFolderBeanList(session, list, folders[i], includeReserved, includeUnsubscribed);
        }
    }

    // gets a list of subfolders
    private static Folder[] getFolderList(final Folder folder, final boolean includeUnsubscribed) throws MessagingException {
        return includeUnsubscribed ? folder.list() : folder.listSubscribed();
    }

    // is this folder to be restricted from the list?
    private static boolean isFolderIncluded(final Folder folder, final boolean includeReserved, final HttpSession session) throws MessagingException {
        return (!(!includeReserved && Util.isReservedFolder(folder.getFullName(), session)));
    }

    // array of (lowercase) characters allowed in a new folder's name
    private static final char[] legalChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '+', ',', '-', ':', '=', '@', '_', '~'};

    static {
        // sort so that Arrays.binarySearch(..) will work
        Arrays.sort(legalChars);
    }

    /**
     * Verify that new folder name is OK.
     *
     * @param  folderName          the name of the new folder.
     * @param  parentName          the full name of the parent folder.
     * @param  errors              add any ActionError to this ActionErrors object.
     * @param  request             the servlet request. Any errors get saved here.
     * @return                     true if this is a legal folder name.
     * @throws MessagingException
     */
    protected static boolean isLegalNewFolder(final String folderName, final String parentName, final ActionErrors errors, final HttpServletRequest request) throws MessagingException {
        final HttpSession session = request.getSession();

        // check if new folder is blank
        if (folderName == null || folderName.trim().equals("")) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.createFolder.newFolder.required"));
            return false;
        }

        // check if parent folder is blank
        if (parentName == null || parentName.trim().equals("")) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.createFolder.parentFolder.required"));
            return false;
        }

        // check if the folder already exists
        final String fullName = parentName + "." + folderName;
        Folder inbox = null;
        List folderBeanList = null;
        try {
            inbox = Util.getFolder(session, "INBOX");
            folderBeanList = ActionsUtil.getFolderBeanList(session, inbox);
        } finally {
            Util.releaseFolder(inbox); //clean up
        }

        final int size = folderBeanList.size();
        for (int i = 0; i < size; i++) {
            final Folder folder = (Folder)folderBeanList.get(i);
            if (fullName.equals(folder.getFullName())) {
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.createFolder.exists", fullName));
                return false;
            }
        }

        // check for length
        if (folderName.length() > Constants.MAXSIZE_FOLDERNAME) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.createFolder.newFolder.toolong", String.valueOf(Constants.MAXSIZE_FOLDERNAME)));
            return false;
        }

        // check if folder name has illegal characters
        final String tempName = folderName.toLowerCase();
        final int length = folderName.length();
        for (int i = 0; i < length; i++) {
            if (Arrays.binarySearch(legalChars, tempName.charAt(i)) < 0) {
                logger.error("checked folder name. illegal character found: " + tempName.charAt(i));
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.createFolder.illegalChar", String.valueOf(tempName.charAt(i))));
                return false;
            }
        }

        return true; // looks good
    }

    /**
     * Verifies that this folder qualifies for deletion. Does not perform
     * saveErrors(..) - you must do that after returning from this method.
     *
     * @return                     true if the folder can validly be deleted,
     *                             false if not.
     */
    protected static boolean isLegalDelete(final Folder folder, final ActionErrors errors, final HttpSession session) throws MessagingException {
        final String deleteFolderName = folder.getFullName();

        // check if the folder to be deleted exists
        if (!folder.exists()) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.deleteFolder.notexist", deleteFolderName));
            return false;
        }

        // check if the folder to be deleted is required for this application
        if (Util.isReservedFolder(deleteFolderName, session)) {
            logger.debug("folder is required for this application");
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.deleteFolder.notallowed", deleteFolderName));
            return false;
        }

        return true; // everything looks good
    }

    /**
     * Converts an array of {@link javax.mail.Address} objects and convert them
     * to a comma seperated sting.
     *
     * @param  addresses           The array of {@link javax.mail.Address}
     *                             objects.
     * @return                     comma seperated string.
     */
    protected static String getAddressString(final Address[] addresses) {
        final StringBuffer buff = new StringBuffer();
        if (addresses != null && addresses.length > 0) {
            for (int i = 0; i < addresses.length; i++) {
                buff.append(addresses[i].toString());
                if (i + 1 < addresses.length)
                    buff.append(", ");
            }
        }
        return buff.toString();
    }

    /**
     * Copies message(s) into a folder. Catches any over-quota error and returns
     * a list of any messages it couldn't copy. If there was an error, it is
     * saved in the ActionErrors parameter.
     *
     * @return                     a List of unfinished messages, in case there
     *                             was an error. If no error, an empty list is
     *                             returned.
     */
    protected static boolean appendMessage(final Message message, final Folder folder, final ActionErrors errors) throws MessagingException {
        logger.debug("> appendMessage start");
        try {
            folder.appendMessages(new Message[]{message});
            logger.debug(".. appended message to " + folder.getFullName());
        } catch (MessagingException me) {
            logger.error("appendMessage failed", me);
            final Exception ex = me.getNextException();
            if (ex instanceof CommandFailedException) {
                logger.error("ex is an instance of CommandFailedException");
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.copyMessage.failed", ex.toString()));
                logger.debug("> appendMessage end");
                return false;
            }
            // not a quota error - throw it back
            logger.error("ex is NOT an instance of CommandFailedException");
            logger.debug("> appendMessage end");
            throw me;
        }
        logger.debug("> appendMessage end");
        return true;
    }

    /**
     * Copies message(s) into a folder. Catches any over-quota error and returns
     * a list of any messages it couldn't copy. If there was an error, it is
     * saved in the ActionErrors parameter.
     *
     * @return                     a List of unfinished messages, in case there
     *                             was an error. If no error, an empty list is
     *                             returned.
     */
    protected static List copyMessages(final List messageList, final Folder folder, final Folder toFolder, final ActionErrors errors) throws MessagingException {
        return copyMessages((Message[])messageList.toArray(new Message[]{}), folder, toFolder, errors);
    }

    /**
     * Copies message(s) into a folder. Catches any over-quota error and returns
     * a list of any messages it couldn't copy. If there was an error, it is
     * saved in the ActionErrors parameter.
     *
     * @return                     a List of unfinished messages, in case there
     *                             was an error. If no error, an empty list is
     *                             returned.
     */
    protected static List copyMessages(final Message[] messages, final Folder folder, final Folder toFolder, final ActionErrors errors) throws MessagingException {
        logger.debug("> copyMessages start");
        final List messageList = new ArrayList(Arrays.asList(messages));
        try {
            for (int i = 0; i < messages.length; i++) {
                try {
                    folder.copyMessages(new Message[]{messages[i]}, toFolder);
                } catch (NullPointerException npe) {
                    // swallow
                }
                logger.debug(".. copied message " + i + " into trash");
                messageList.remove(messages[i]);
                logger.debug(".. removed message " + i + " from unfinished list");
            }
        } catch (MessagingException me) {
            logger.error(me.toString());
            logger.error(".. out of quota? Unfinished messages: " + messageList.size());

            Exception ex;
            while ((ex = me.getNextException()) != null) {
                if (ex instanceof com.sun.mail.iap.CommandFailedException) {
                    logger.error("ex is an instance of CommandFailedException");
                    errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.copyMessage.failed", ex.toString()));
                    logger.debug("> copyMessages end");
                    return messageList;
                }
            }
            // not a quota error - throw it back
            logger.error("ex is NOT an instance of CommandFailedException");
            logger.debug("> copyMessages end");
            throw me;
        }
        logger.debug("> copyMessages end");
        return messageList;
    }

    /**
     * Returned folder is always open.
     */
    protected static Folder createFolder(final Folder folder) throws MessagingException {
        logger.debug("creating new folder: " + folder.getFullName());
        folder.create(Folder.HOLDS_FOLDERS | Folder.HOLDS_MESSAGES);

        if (!folder.isOpen()) {
            folder.open(Folder.READ_WRITE);
        }

        // Auto subscribe to the newly created folder for the user.
        folder.setSubscribed(true);

        return folder;
    }

    /**
     * Convert an array of Messages into a List of valid messages.
     * The returned List may be smaller than the passed in array, if
     * one or more of the Messages is null or expunged.
     */
    protected static List buildMessageList(final Message[] messages) throws MessagingException {
        return new ArrayList(Arrays.asList(messages));
    }

    /**
     * Returns a {@link java.util.List} of all folders a user is subscribed to.
     *
     * @param  store               The user's {@link javax.mail.Store}.
     * @return                     The folders to which a user is subscribed.
     * @throws MessagingException  thrown by JavaMail.
     */
    public static List getSusbscribedFolders(final Store store) throws MessagingException {
        final List list;
        final Folder folder = store.getDefaultFolder();
        final Folder[] subscribedFolders = folder.listSubscribed("*");
        // Be careful about users with zero subscribed folders.
        if (subscribedFolders != null) {
            list = new ArrayList(subscribedFolders.length);
            for (int i=0; i < subscribedFolders.length; i++) {
                if (subscribedFolders[i].isSubscribed()) {
                    list.add(subscribedFolders[i]);
                }
            }
            //list = Arrays.asList(subscribedFolders);
        } else {
            list = Collections.EMPTY_LIST;
        }
        return list;
    }

    /**
     * Returns a {@link java.util.List} of all folders in a user's INBOX. I'd
     * like to list <b>all</b> folders a user has access to but that simply
     * takes too long.
     *
     * @param  store               The user's {@link javax.mail.Store}.
     * @return                     The folders in a user's INBOX.
     * @throws MessagingException  thrown by JavaMail.
     */
    public static List getFolders(final Store store) throws MessagingException {
        final Folder folder;
        final List list;

        // XXX: Correct but slow.
        //folder = store.getDefaultFolder();

        // XXX: Less correct but faster.
        folder = store.getFolder("INBOX");
        if (Util.traceProtocol) {
            Util.addProtocolMarkers(folder, (new Exception()).getStackTrace());
        }

        list = getFolders(folder);

        return list;
    }

    /**
     * Gets a {@link List} of {@link Folder}s.
     *
     * @param  folder              the base folder to get a list from, this
     *                             instance may be closed.
     * @return                     A list of folders.
     * @throws MessagingException  from JavaMail {@link Folder#list(String)}.
     */
    public static List getFolders(final Folder folder) throws MessagingException {
        final List list;
        final Folder[] folders;
        list = new ArrayList();
        list.add(folder); // XXX If we switch to not using the INBOX we need to remove this.
        folders = folder.list("*");
        if (folders != null) {
            list.addAll(Arrays.asList(folders));
        } else {
            // XXX When we switch to not using the INBOX
            //list = Collections.EMPTY_LIST;
        }

        return list;
    }

    /**
     * Run a comparison to determine a folder's sort
     * Returns a comparator.
     */
    protected static Comparator fetchSort(final String sort) {

        final Comparator comparator;

        if ("dateDN".equals(sort)) {
            comparator = new DateSort();
        } else if ("dateUP".equals(sort)) {
            comparator = new DateSortU();
        } else if ("sizeDN".equals(sort)) {
            comparator = new SizeSort();
        } else if ("sizeUP".equals(sort)) {
            comparator = new SizeSortU();
        } else if ("subDN".equals(sort)) {
            comparator = new SubjectSortU();
        } else if ("subUP".equals(sort)) {
            comparator = new SubjectSort();
        } else if ("toDN".equals(sort)) {
            comparator = new ToSort();
        } else if ("toUP".equals(sort)) {
            comparator = new ToSortU();
        } else if ("fromDN".equals(sort)) {
            comparator = new FromSort();
        } else if ("fromUP".equals(sort)) {
            comparator = new FromSortU();
        } else {
            comparator = new DateSort();
        }

        return comparator;
    }
}
