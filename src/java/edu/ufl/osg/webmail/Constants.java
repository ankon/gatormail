/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002, 2003 William A. McArthur, Jr.
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

package edu.ufl.osg.webmail;

import edu.ufl.osg.webmail.util.Util;

import javax.servlet.http.HttpSession;
import javax.mail.Folder;
import javax.mail.MessagingException;

/**
 * Constants for the WebMail application.
 *
 * @author sandymac
 * @author drakee
 * @version $Revision: 1.3 $
 */
public final class Constants {
    /** The application scoped key for the JavaMail Session provider. */
    public static final String MAIL_SESSION_PROVIDER = "sessionProvider";

    /** The application scoped key for the JavaMail Session provider. */
    public static final String PREFERENCES_PROVIDER = "preferencesProvider";

    /** The session scoped key for a user's JavaMail session. */
    public static final String MAIL_SESSION_KEY = "session";

    /** The session scoped key for a user's JavaMail store. */
    public static final String MAIL_STORE_KEY = "store";

    /** Session scoped key for the user's login info. */
    public static final String USER_KEY = "user";

    /** Is this user logging in? */
    public static final String LOGGING_IN = "logging in";

    /** request scoped key for message displaying result of an action */
    public static final String RESULT = "result";

    /** request scoped key for attachments list */
    public static final String ATTACHMENT_LIST = "attachList";

    /** session scoped key for attachment map */
    public static final String ATTACHMENT_MAP = "attachMap";

    /** session scoped key for addressbook list */
    public static final String ADDRESS_LIST = "addressList";

    /** request/parameter scoped key indicating a message was deleted from trash */
    public static final String DELETE_FOREVER = "deleteForever";

    /** max sizes for user-entered fields. */
    public static final int MAXSIZE_NAME = 30;
    public static final int MAXSIZE_EMAIL = 40;
    public static final int COMPOSE_BODY_WIDTH = 80;
    public static final int MAXSIZE_FOLDERNAME = 30;

    /** name for the IMAP box to put trash within */
    public static final String TRASH_FOLDER = "Trash";

    /** name for the IMAP box to put sent messages within */
    public static final String SENT_FOLDER = "Sent";

    /** request scoped key for a list of messages */
    public static final String MESSAGE_LIST = "messageList";

    /** request scoped key for delete action */
    public static final String DELETE_ACTION = "deleteAction";

    public static final String SAVE_ADDRESS_LIST = "saveAddressList";

    public static final String ADDRESSBK_LIMIT = "addressBkLimit";
    public static final String ADDRESSBK_USAGE = "addressBkUsage";
    public static final String IS_SELECTED = "isSelected";

    /** session scoped key for compose form saved when user's session timed out */
    public static final String SAVED_COMPOSE_FORM = "savedComposeForm";

    public static final String DELETE_ATTACHMENT = "deleteAttachment";
    public static final String MESSAGE_FLAG_HAS_ATTACHMENT = "HasAttachment";
    public static final String MESSAGE_FLAG_NO_ATTACHMENT = "NoAttachment";

    public static final String MAILBEAN_KEY = "mailBean";

    public static String getTrashFolderFullname(final HttpSession session) throws MessagingException {
        String trashFolderFullName;
        synchronized (session) {
            trashFolderFullName = (String)session.getAttribute("trashFolderFullname");
            if (trashFolderFullName == null) {
                final Folder inbox = Util.getFolder(session, "INBOX");
                Util.releaseFolder(inbox);

                trashFolderFullName = inbox.getFolder(TRASH_FOLDER).getFullName();
                session.setAttribute("trashFolderFullname", trashFolderFullName);
            }
        }
        return trashFolderFullName;
    }

    public static String getSentFolderFullname(final HttpSession session) throws MessagingException {
        String sentFolderFullname;
        synchronized (session) {
            sentFolderFullname = (String)session.getAttribute("sentFolderFullname");
            if (sentFolderFullname == null) {
                final Folder inbox = Util.getFolder(session, "INBOX");
                Util.releaseFolder(inbox);

                sentFolderFullname = inbox.getFolder(SENT_FOLDER).getFullName();
                session.setAttribute("sentFolderFullname", sentFolderFullname);
            }
        }
        return sentFolderFullname;
    }
}
