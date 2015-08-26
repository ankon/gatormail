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
import edu.ufl.osg.webmail.Constants;
import edu.ufl.osg.webmail.User;
import edu.ufl.osg.webmail.forms.FolderForm;
import edu.ufl.osg.webmail.prefs.PreferencesProvider;
import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.AddressTerm;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.HeaderTerm;
import javax.mail.search.NotTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;
import javax.mail.search.BodyTerm;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Sets up Folder/Message listing view.
 *
 * @author sandymac
 * @author drakee
 * @version $Revision: 1.17 $
 */
public class FolderAction extends Action {
    private static final Logger logger = Logger.getLogger(FolderAction.class.getName());

    private static final List filterChoices;

    static {
        final List list = new ArrayList();
        list.add("none");
        list.add("subjectAndSender");
        list.add("subject");
        list.add("sender");
        list.add("toRecipient");
        list.add("allRecipients");
        list.add("body");
        list.add("new");
        list.add("seen");
        list.add("replied");
        list.add("hasAttachment");
        list.add("noAttachment");
        list.add("addressBook");
        list.add("notAddressBook");
        list.add("flaggedJunk");
        list.add("notFlaggedJunk");
        filterChoices = Collections.unmodifiableList(list);
    }

    /**
     * Sets up the request enviroment for the folder view. The current folder
     * and a List of messages are put in the request attributes. If there is no
     * folder parameter of the request URL forward the user to their INBOX.
     *
     * @param mapping  The ActionMapping used to select this
     *                 instance
     * @param form     The optional ActionForm bean for this request
     *                 (if any)
     * @param request  The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return An ActionForward instance to either their
     *         inbox or the view.
     * @throws Exception if the application business logic throws an
     *                   exception
     */
    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        logger.debug("=== FolderAction.execute() begin ===");
        ActionsUtil.checkSession(request);

        final FolderForm folderForm = (FolderForm)form;
        
        // Get the current Sort
        final Comparator comparator;
        if (folderForm != null) {
            request.setAttribute("folderSort", folderForm.getSort());
            final String sort = folderForm.getSort().trim();
            comparator = ActionsUtil.fetchSort(folderForm.getSort());
            folderForm.setSort(sort);
        } else {
            comparator = ActionsUtil.fetchSort(null);
        }

        // Get the current Folder
        final Folder folder = ActionsUtil.fetchFolder(folderForm, request);
        final HttpSession session;
        try {
            request.setAttribute("folder", folder);

            if (folder == null) {
                return mapping.findForward("inbox");
            }

            session = request.getSession();
            final User user = Util.getUser(session);
            final PreferencesProvider pp = (PreferencesProvider)getServlet().getServletContext().getAttribute(Constants.PREFERENCES_PROVIDER);
            final Properties prefs = pp.getPreferences(user, session);

            // Get valid Messages in the folder
            final List messageList;
            final List addressBookList = Util.getAddressList(request.getSession()); // TODO: Only fetch when filtering

            final SearchTerm searchTerms = buildSearchFilter(folderForm.getFilterType(), folderForm.getFilter(), addressBookList, prefs);

            Message[] messages = folder.search(searchTerms);
            List ml = Arrays.asList(messages);
            prefetchMessageEnvelope(folder, ml);
            Collections.sort(ml, comparator); // XXX Sort on user's preferences

            if ((folderForm.getFilterType() == null || "none".equals(folderForm.getFilterType())) && folderForm.getPage().intValue() > 0) {
                int start = Math.min(messages.length, Math.max(0, (folderForm.getPage().intValue() - 1) * 25));
                int end = Math.min(messages.length, start + 25);
                int pages = (int)Math.ceil(messages.length / 25d);
                messages = (Message[])Arrays.asList(messages).subList(start, end).toArray(new Message[0]);
                request.setAttribute("folderListPage", new Integer(folderForm.getPage().intValue()));
                request.setAttribute("folderListPages", new Integer(pages));
             }
            if (folderForm != null) {
                request.setAttribute("folderListFilterType", folderForm.getFilterType());
                request.setAttribute("folderListFilter", folderForm.getFilter());

            }
            prefetchEverything(folder, messages);
            messageList = ActionsUtil.buildMessageList(messages);

            flagMessagesWithAttachments(messageList);

            // Populate the request attributes
            request.setAttribute("messages", messageList);
        } finally {
            Util.releaseFolder(folder); // clean up
        }

        final Folder inbox = Util.getFolder(session, "INBOX");
        try {
            // Populate Quota info
            final Set quotaSet = ActionsUtil.getCachedQuotas(inbox, session);
            request.setAttribute("quotaSet", quotaSet);
        } finally {
            Util.releaseFolder(inbox); // clean up
        }

        // Populate the folderNameList
        final List folderList = ActionsUtil.getSusbscribedFolders(Util.getMailStore(session));
        request.setAttribute("folderBeanList", folderList);

        logger.debug("=== FolderAction.execute() end ===");
        return mapping.findForward("success");
    }

    private static void prefetchEverything(final Folder folder, final Message[] messages) throws MessagingException {
        final FetchProfile fp = new FetchProfile();
        fp.add(UIDFolder.FetchProfileItem.UID);
        fp.add(FetchProfile.Item.FLAGS);
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add("References");
        fp.add("In-Reply-To");
        fp.add("X-Spam-Level");
        folder.fetch(messages, fp);
    }

    private static void prefetchMessageFlags(final Folder folder, final Message[] msgs) throws MessagingException {
        // Prefetch the flags for all messages.
        final FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.FLAGS);
        folder.fetch(msgs, fp);
    }

    /**
     * Prefetches the {@link FetchProfile.Item#ENVELOPE} and {@link
     * UIDFolder.FetchProfileItem#UID} for each {@link Message}. All messages in
     * <code>messageList</code> must be from <code>folder</code>.
     *
     * @param folder      the folder for which to prefetch from.
     * @param messageList the list of {@link javax.mail.Message} objects.
     * @throws MessagingException  if there is a problem with {@link
     *                             Folder#fetch}.
     * @throws ArrayStoreException if the <code>messageList</code> cannot be
     *                             converted to a <code>{@link
     *                             Message}[]</code>.
     */
    private static void prefetchMessageEnvelopeUID(final Folder folder, final List messageList) throws MessagingException, ArrayStoreException {
        // Prefetch the date headers for all messages.
        final FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(UIDFolder.FetchProfileItem.UID);
        final Message[] msgs = (Message[])messageList.toArray(new Message[]{});
        folder.fetch(msgs, fp);
    }

    private static void prefetchMessageEnvelope(final Folder folder, final List messageList) throws MessagingException, ArrayStoreException {
        // Prefetch the date headers for all messages.
        final FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        final Message[] msgs = (Message[])messageList.toArray(new Message[]{});
        folder.fetch(msgs, fp);
    }

    /**
     * Prefetches the <code>References</code> and <code>In-Reply-To</code> {@link IMAPFolder.FetchProfileItem#HEADERS}
     * for each {@link Message}. All messages in <code>messageList</code> must be from <code>folder</code>.
     *
     * @param folder      the folder for which to prefetch from.
     * @param messageList the list of {@link javax.mail.Message} objects.
     * @throws MessagingException  if there is a problem with {@link
     *                             Folder#fetch}.
     * @throws ArrayStoreException if the <code>messageList</code> cannot be
     *                             converted to a <code>{@link
     *                             Message}[]</code>.
     */
    private static void prefetchThreadingMessageHeaders(final Folder folder, final List messageList) throws MessagingException, ArrayStoreException {
        // Prefetch the threading headers for all messages.
        final FetchProfile fp = new FetchProfile();
        //fp.add(IMAPFolder.FetchProfileItem.HEADERS);
        fp.add("References");
        fp.add("In-Reply-To");
        final Message[] msgs = (Message[])messageList.toArray(new Message[]{});
        folder.fetch(msgs, fp);
    }

    /**
     * Prefetches the <code>X-Spam-Leve</code> {@link IMAPFolder.FetchProfileItem#HEADERS} for each
     * {@link Message}. All messages in <code>messageList</code> must be from <code>folder</code>.
     *
     * @param folder      the folder for which to prefetch from.
     * @param messageList the list of {@link javax.mail.Message} objects.
     * @throws MessagingException  if there is a problem with {@link
     *                             Folder#fetch}.
     * @throws ArrayStoreException if the <code>messageList</code> cannot be
     *                             converted to a <code>{@link
     *                             Message}[]</code>.
     */
    private static void prefetchJunkMessageHeaders(final Folder folder, final List messageList) throws MessagingException, ArrayStoreException {
        // Prefetch the spam headers for all messages.
        final FetchProfile fp = new FetchProfile();
        //fp.add(IMAPFolder.FetchProfileItem.HEADERS);
        fp.add("X-Spam-Level");
        //fp.add("X-Spam-Status");
        final Message[] msgs = (Message[])messageList.toArray(new Message[]{});
        folder.fetch(msgs, fp);
    }

    private static void flagMessagesWithAttachments(final List messages) {
        final Iterator iter = messages.iterator();
        int msgsFlagged = 0;
        while (iter.hasNext()) {
            try {
                if (flagMessageWithAttachments((Message)iter.next())) {
                    msgsFlagged++;
                }
            } catch (Exception e) {
                logger.warn("Problem figuring out attachment state of message", e);
            }
            // Break out after the first 50 messages so we don't spend all day flagging messages.
            // The next time they view that folder it will flag another 50 messages if they need it.
            if (msgsFlagged > 50) {
                break;
            }
        }
    }

    /**
     * @param message The message to look for atachments in.
     * @return true if the message was flagged.
     * @throws MessagingException JavaMail problem
     */
    private static boolean flagMessageWithAttachments(final Message message) throws MessagingException {
        boolean hasAttachment = false;
        final boolean seen = message.isSet(Flags.Flag.SEEN);
        Flags flags = message.getFlags();

        if (flags.contains(Constants.MESSAGE_FLAG_HAS_ATTACHMENT) || flags.contains(Constants.MESSAGE_FLAG_NO_ATTACHMENT)) {
            return false;
        }

        // No pre-existing flags

        if (message.isMimeType("text/*")) {
            hasAttachment = false;

        } else if (message.isMimeType("multipart/*")) {
            try {
                final MimeMultipart mmp = (MimeMultipart)message.getContent();
                hasAttachment = Util.hasAttachment(mmp);
            } catch (Exception e) {
                // Do nothing
            }
        }

        if (hasAttachment) {
            flags = new Flags(Constants.MESSAGE_FLAG_HAS_ATTACHMENT);
            message.setFlags(flags, true);
        } else {
            flags = new Flags(Constants.MESSAGE_FLAG_NO_ATTACHMENT);
            message.setFlags(flags, true);
        }

        // Reset the seen state if we need to.
        if (!seen) {
            message.setFlag(Flags.Flag.SEEN, false);
        }

        return true;
    }

    private static SearchTerm buildSearchFilter(final String type, final String filter, final List addressBookList, final Properties prefs) {
        SearchTerm searchTerm = null;
        if (filter == null || type == null || type.equals("none")) {
            searchTerm = null;

        } else if (type.equals("subjectAndSender")) {
            final FromStringTerm fst = new FromStringTerm(filter);
            final SubjectTerm ist = new SubjectTerm(filter);
            searchTerm = new OrTerm(fst, ist);

        } else if (type.equals("subject")) {
            searchTerm = new SubjectTerm(filter);

        } else if (type.equals("sender")) {
            searchTerm = new FromStringTerm(filter);

        } else if (type.equals("toRecipient")) {
            searchTerm = new RecipientStringTerm(Message.RecipientType.TO, filter);

        } else if (type.equals("allRecipients")) {
            final SearchTerm[] orTerms = new SearchTerm[3];
            orTerms[0] = new RecipientStringTerm(Message.RecipientType.TO, filter);
            orTerms[1] = new RecipientStringTerm(Message.RecipientType.CC, filter);
            orTerms[2] = new RecipientStringTerm(Message.RecipientType.BCC, filter);
            searchTerm = new OrTerm(orTerms);

        } else if (type.equals("body")) {
            searchTerm = new BodyTerm(filter);

        } else if (type.equals("new")) {
            final Flags f = new Flags();
            f.add(Flags.Flag.SEEN);
            searchTerm = new FlagTerm(f, false);

        } else if (type.equals("seen")) {
            final Flags f = new Flags();
            f.add(Flags.Flag.SEEN);
            searchTerm = new FlagTerm(f, true);

        } else if (type.equals("replied")) {
            final Flags f = new Flags();
            f.add(Flags.Flag.ANSWERED);
            searchTerm = new FlagTerm(f, true);

        } else if (type.equals("hasAttachment")) {
            // TODO: Rewrite this using FlagTerm
            searchTerm = new AttachmentTerm();

        } else if (type.equals("noAttachment")) {
            // TODO: Rewrite this using FlagTerm
            searchTerm = new NotTerm(new AttachmentTerm());

        } else if (type.equals("addressBook")) {
            if (addressBookList.size() != 0) {
                final FromEmailTerm[] fromAddresses = new FromEmailTerm[addressBookList.size()];
                for (int i = 0; i < fromAddresses.length; i++) {
                    fromAddresses[i] = new FromEmailTerm((InternetAddress)addressBookList.get(i));
                }
                searchTerm = new OrTerm(fromAddresses);
            }

        } else if (type.equals("notAddressBook")) {
            if (addressBookList.size() != 0) {
                final FromEmailTerm[] fromAddresses = new FromEmailTerm[addressBookList.size()];
                for (int i = 0; i < fromAddresses.length; i++) {
                    fromAddresses[i] = new FromEmailTerm((InternetAddress)addressBookList.get(i));
                }
                searchTerm = new NotTerm(new OrTerm(fromAddresses));
            }

        } else if (type.equals("flaggedJunk")) {
            final String junkPattern = buildJunkPattern(prefs);
            if (junkPattern != null) {
                searchTerm = new HeaderTerm("X-UFL-Spam-Level", junkPattern);
            }

        } else if (type.equals("notFlaggedJunk")) {
            final String junkPattern = buildJunkPattern(prefs);
            if (junkPattern != null) {
                searchTerm = new NotTerm(new HeaderTerm("X-UFL-Spam-Level", junkPattern));
            }

        } else {
            searchTerm = new FlagTerm(new Flags(Flags.Flag.DELETED), false);

        }

        if (searchTerm == null) {
            searchTerm = new FlagTerm(new Flags(Flags.Flag.DELETED), false);
        } else {
            searchTerm = new AndTerm(new FlagTerm(new Flags(Flags.Flag.DELETED), false), searchTerm);
        }
        return searchTerm;
    }

    private static String buildJunkPattern(final Properties prefs) {
        final int junkThreshold = Integer.valueOf(prefs.getProperty("message.junk.threshold", "8")).intValue();
        final String junkPattern;
        if (junkThreshold > 0) {
            junkPattern = "***************************************".substring(0, junkThreshold);
        } else {
            junkPattern = null;
        }
        return junkPattern;
    }

    private static class AttachmentTerm extends SearchTerm {
        public boolean match(final Message message) {
            try {
                return Util.hasAttachment(message);
            } catch (MessagingException e) {
                return false;
            }
        }

    }

    /**
     * From an address in the users address book.
     */
    private static class FromEmailTerm extends AddressTerm {
        private FromEmailTerm(final InternetAddress address) {
            super(address);
        }

        public boolean match(final Message message) {
            final Address[] address;
            try {
                address = message.getFrom();
            } catch (Exception _ex) {
                return false;
            }
            if (address == null) {
                return false;
            }
            for (int i = 0; i < address.length; i++) {
                if (address[i] instanceof InternetAddress) {
                    final InternetAddress iAddr = (InternetAddress)address[i];
                    if (iAddr.getAddress().toLowerCase().equals(((InternetAddress)getAddress()).getAddress().toLowerCase())) {
                        return true;
                    }

                }

            }

            return false;
        }

    }

    public static List getFilterChoices() {
        return Collections.unmodifiableList(filterChoices);
    }
}
