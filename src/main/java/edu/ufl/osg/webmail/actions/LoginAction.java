/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002-2004 William A. McArthur, Jr.
 * Copyright (C) 2003,2006 The Open Systems Group / University of Florida
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

import edu.ufl.osg.webmail.Constants;
import edu.ufl.osg.webmail.SessionProvider;
import edu.ufl.osg.webmail.User;
import edu.ufl.osg.webmail.data.DAOFactory;
import edu.ufl.osg.webmail.data.UserInfoDAO;
import edu.ufl.osg.webmail.data.UserInfoDAOException;
import edu.ufl.osg.webmail.prefs.PreferencesProvider;
import edu.ufl.osg.webmail.util.Util;
import edu.ufl.osg.webmail.util.RequestTimerFilter;
import edu.ufl.osg.webmail.util.FolderCloserFilter;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Store;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;


/**
 * Login controller.
 *
 * @author sandymac
 * @author drakee
 * @version $Revision: 1.9 $
 */
public class LoginAction extends Action {
    private static final Logger logger = Logger.getLogger(LoginAction.class.getName());

    /**
     * Process a user's login attempt. If the login is successful then a
     * JavaMail session and Store are created and added to the servlet session
     * and the user is forwarded to their inbox. If the login is unsuccessful
     * then the user is sent back to the login form.
     *
     * @param  mapping             The ActionMapping used to select this
     *                             instance
     * @param  form                The optional ActionForm bean for this request
     *                             (if any)
     * @param  request             The HTTP request we are processing
     * @param  response            The HTTP response we are creating
     * @return                     An ActionForward instance either for the
     *                             inbox or back to the login page.
     * @throws Exception           if the application business logic throws an
     *                             exception
     */
    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        // XXX: Yea, this is could be done better.
        try {
            ActionsUtil.checkSession(request);

            return mapping.findForward("inbox");
        } catch (Exception e) {
            // Nothin, were fine
        }

        // Get the JavaMail session provider
        final SessionProvider sessionProvider = DAOFactory.getInstance().getSessionProvider();

        ActionErrors errors = new ActionErrors();

        final String action = (String)PropertyUtils.getSimpleProperty(form, "action");
        final String username = (String)PropertyUtils.getSimpleProperty(form, "username");
        final String password = (String)PropertyUtils.getSimpleProperty(form, "password");

        // XXX: I'm not sure this is right? What if the user presses enter w/o clicking the button
        if ((action == null) && (username == null) && (password == null) && (request.getAttribute("UF_GatorLinkID") == null)) {
            if (RequestTimerFilter.isOverLimit()) {
                return mapping.findForward("overLimit");
            } else {
                return mapping.findForward("login");
            }
        }

        final User user = sessionProvider.getUser(username, password, request);
//         Authenticator auth = sessionProvider.getAuthenticator(user);
        Store mailStore = null;
        try {
            mailStore = user.getMailStore();
        } catch (NoSuchProviderException nspe) {
            // This will be thrown when JavaMail is too old or something.
            // commonly there are JavaMail jars either in the JVM or the container lib
            throw nspe; // TODO: Redirect to a page to inform the admin of the problem.
        }

        // Check the login
        try {
            mailStore.connect();
        } catch (AuthenticationFailedException afe) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("login.authentication.failed", afe.getMessage()));
        } catch (MessagingException me) {
            final Exception e = me.getNextException();

            if (e instanceof UnknownHostException) {
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.login.mailhost.unknownhostexception", e.getMessage()));
            } else if (e instanceof NoRouteToHostException) {
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.login.mailhost.noroutetohostexception", e.getMessage()));
            } else if (e instanceof ConnectException) {
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.login.mailhost.connectexception", e.getMessage()));
            } else {
                logger.error("I don't recognize this error: " + me);
                //throw me; // I don't know what to do with it
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.login.mailhost.default", e.getMessage()));
            }
        }

        // Back to login if errors
        if (!errors.isEmpty()) {
            saveErrors(request, errors);
            return mapping.getInputForward();
        }

        // check for presence of INBOX
        Folder inbox = null;
        try {
            inbox = mailStore.getFolder("INBOX");
            FolderCloserFilter.closeFolder(inbox);
            if (Util.traceProtocol) {
                Util.addProtocolMarkers(inbox, (new Exception()).getStackTrace());
            }
            if (inbox == null || !inbox.exists()) {
                return mapping.findForward("noInbox");
            } else {
                logger.debug("INBOX found OK. type: " + inbox.getType());
                // It is necessary not only to check inbox.exists(), but also
                // to try opening it. Otherwise we may not catch an error if
                // the mail server is down.
                if (!inbox.isOpen()) {
                    inbox.open(Folder.READ_ONLY);
                    inbox.setSubscribed(true);
                    if (Util.traceProtocol) {
                        Util.addProtocolMarkers(inbox, (new Exception()).getStackTrace());
                    }
                    inbox.close(false);
                    logger.debug("INBOX opened and closed");
                }
            }
        } catch (Exception e) {
            logger.error("error fetching INBOX: " + e.toString());
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.login.mailhost.fetch.inbox", e.getMessage()));
            saveErrors(request, errors);
            return mapping.findForward("errorLogout");
        }

        // Save our logged-in user in the session
        final HttpSession session = request.getSession();
        synchronized (session) {
            session.setAttribute(Constants.LOGGING_IN, Boolean.TRUE);
            session.setAttribute(Constants.USER_KEY, user);
//             session.setAttribute(Constants.MAIL_SESSION_KEY, mailSession);
//             session.setAttribute(Constants.MAIL_STORE_KEY, mailStore);
        }

        // initialize necessary session objects for this user
        errors = initializeObjects(user, errors, session);

        // logout and show error page, if errors
        if (!errors.isEmpty()) {
            saveErrors(request, errors);
            return mapping.findForward("errorLogout");
        }

        final PreferencesProvider pp = (PreferencesProvider) getServlet().getServletContext().getAttribute(Constants.PREFERENCES_PROVIDER);
        final Properties prefs = pp.getPreferences(user, session);
        prefs.setProperty("user.login.last", new Date().toString());

        session.setAttribute(Constants.LOGGING_IN, Boolean.FALSE);

        if (PreferencesAction.hasActiveVacationScript(session)) {
            session.setAttribute("vacationEnabled", Boolean.TRUE);            
        }

        // if user's session timed out while composing a message
        if (session.getAttribute(Constants.SAVED_COMPOSE_FORM) != null) {
            return mapping.findForward("composeResume");
        }

        return mapping.findForward("success");
    }

    /**
     * The names of reserved (special) folders.
     */
    private static final String[] reservedFolderNames = {Constants.TRASH_FOLDER, Constants.SENT_FOLDER};

    /**
     * Initialize session objects, such as folderList, user, trash folder. Build
     * an ActionErrors object containing any errors caught in the process.
     */
    private static ActionErrors initializeObjects(final User user, final ActionErrors errors, final HttpSession session) {
        // if any of these fail, log user out & ask him to try again
        try {
            // initialize the user object
            setUserInfo(user);
        } catch (Exception e) {
            logger.error(Util.buildErrorMessage(e, user));
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.userinfo.dao.exception"));
        }

        try {
            // make sure the address list is in session
            Util.getAddressList(session);
        } catch (Exception e) {
            logger.error(Util.buildErrorMessage(e, user));
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.addressbk.dao.exception"));
        }

        // initialize trash & sent folders
        for (int i = 0; i < reservedFolderNames.length; i++) {
            logger.debug("looking for " + reservedFolderNames[i]);
            // keep track of any error in finding/creating the reserved folder
            boolean errorFlag = false;
            try {
                // make sure there is a Reserved folder. If not, creates one.
                final Folder reservedFolder = getReservedFolder(session, reservedFolderNames[i]);
                if (reservedFolder == null) {
                    errorFlag = true;
                }
                Util.releaseFolder(reservedFolder);
            } catch (Exception e) {
                logger.error(Util.buildErrorMessage(e, user));
                errorFlag = true;
            }
            if (errorFlag) { // save error if there was one
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.folder.reserved.required", reservedFolderNames[i]));
            }
        }

        return errors;
    }


    /**
     * Always returns the reserved folder, except return null if it cannot be
     * created. Use this to initialize folders, such as Trash and Sent, making
     * sure they get created if not already existing.
     *
     * Returned folder is always open.
     *
     * If it's not there, we don't want to use Util.getFolder() and cause a
     * MessagingException.
     *
     * After running this, you can more efficiently call
     * <code>Util.getFolder(session, Constants.TRASH_FOLDER_FULLNAME)<code>.
     */
    private static Folder getReservedFolder(final HttpSession session, final String folderName) throws MessagingException {

        // should throw MessagingException if INBOX doesn't exist
        final Folder rootFolder = Util.getFolder(session, "INBOX");
        final Folder reservedFolder = rootFolder.getFolder(folderName);
        Util.releaseFolder(rootFolder); // clean up

        if (reservedFolder == null) {
            return null;
        }

        if (reservedFolder.exists()) {
            // Make sure we get new messages.
            if (!reservedFolder.isOpen()) {
                reservedFolder.open(Folder.READ_WRITE);
            }
            return reservedFolder; // good - it's there
        } else {
            // reserved folder does not exist - let's create it
            ActionsUtil.flushMailStoreGroupCache(session);
            return ActionsUtil.createFolder(reservedFolder);
        }
    }


    /**
     * Make sure the address book is set in the user's session.
     */
    private static void setUserInfo(final User user) throws UserInfoDAOException {
        final UserInfoDAO userInfoDAO = DAOFactory.getInstance().getUserInfoDAO();
        userInfoDAO.setUserInfo(user);
        logger.debug("set user: " + user);
    }
}
