/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002, 2003 William A. McArthur, Jr.
 * Copyright (C) 2003 The Open Systems Group / University of Florida
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

import edu.ufl.osg.webmail.Constants;
import edu.ufl.osg.webmail.beans.ResultBean;
import edu.ufl.osg.webmail.forms.DeleteMessageForm;
import edu.ufl.osg.webmail.forms.FolderForm;
import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Comparator;
import java.util.List;

/**
 * Deletes a Message. If the deleteForever switch is not set in the form, the message
 * is moved to the Trash.  If the message cannot be moved (i.e. the copy operation
 * fails due to a full mailbox), the user is presented with a page that allows them
 * to delete the message without saving to Trash.
 *
 * If the deleteForever switch is set in the form, the message is permanently deleted.
 *
 * @author drakee
 * @author sandymac
 * @version $Revision: 1.7 $
 */
public class DeleteMessageAction extends FolderAction {
    private static final Logger logger = Logger.getLogger(DeleteMessageAction.class.getName());

    /**
     * Sets up the request enviroment for the folder view. The current folder
     * and a List of messages are put in the request attributes. If there is no
     * folder parameter of the request URL forward the user to their INBOX.
     *
     * @param  mapping             The ActionMapping used to select this
     *                             instance
     * @param  form                The optional ActionForm bean for this request
     *                             (if any)
     * @param  request             The HTTP request we are processing
     * @param  response            The HTTP response we are creating
     * @return                     An ActionForward instance to either their
     *                             inbox or the view.
     * @throws Exception           if the application business logic throws an
     *                             exception
     */
    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        logger.debug("=== DeleteMessageAction.execute() begin ===");
        ActionsUtil.checkSession(request);
        final ActionErrors errors = new ActionErrors();
        final HttpSession session = request.getSession();

        // Open the folder
        Folder folder = null;
        try {
            folder = ActionsUtil.fetchFolder(form, request);
            request.setAttribute("folder", folder);

            // Find the message
            final Message message = ActionsUtil.fetchMessage(form, folder);
            logger.debug("message: " + message);
            if (message == null) {
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.message.notexist"));
                saveErrors(request, errors);
                return mapping.findForward("folder");
            }

            // figure out message to show on next page.
            // this must be done before we delete the current message.
            
            // Get the current Sort
        
            final FolderForm folderForm = (FolderForm)form;
            final String sort = folderForm.getSort().trim();
            final Comparator comparator = ActionsUtil.fetchSort(sort);
            request.setAttribute("folderSort", folderForm.getSort());

            final Message nextMessage = ActionsUtil.fetchNextMessage(message, comparator);

            // do the deleting
            final DeleteMessageForm deleteMessageForm = (DeleteMessageForm)form;
            final boolean deleteForever = deleteMessageForm.getDeleteForever();
            logger.debug("deleteForever: " + deleteForever);
            if (!deleteMessage(request, message, deleteForever, errors)) {
                saveErrors(request, errors);
                return mapping.findForward("errorCopyToTrash");
            }

            // success message for next page
            final ResultBean result = new ResultBean();
            if (deleteForever) {
                result.setMessage(Util.getFromBundle("deleteForever.result.single.success"));
            } else {
                result.setMessage(Util.getFromBundle("delete.result.single.success"));
            }
            request.setAttribute(Constants.RESULT, result);

            // there is no next message...
            if (nextMessage == null) { // go to folder view
                logger.debug("=== DeleteMessageAction.execute() end ===");
                return mapping.findForward("folder");
            } else { // go to message view for next message
                // mark message as read
                nextMessage.setFlag(Flags.Flag.SEEN, true);
                request.setAttribute("message", nextMessage);

                final List folderList = ActionsUtil.getSusbscribedFolders(Util.getMailStore(session));
                request.setAttribute("folderBeanList", folderList);

                logger.debug("=== DeleteMessageAction.execute() end ===");
                return mapping.findForward("success");
            }
        } finally {
            Util.releaseFolder(folder); // clean up
        }
    }

    /**
     * Deletes a message and moves it to the trash.
     * <p>Callers should check the result and deal with the errors if this returns false. eg:
     * <code>saveErrors(request, errors);return mapping.findForward("errorCopyToTrash");</code></p>
     *
     * @param request used to attach a {@link List} of unfinished messages to {@link Constants.MESSAGE_LIST}.
     * @param message the message to delete.
     * @param deleteForever if true then don't move message to the trash.
     * @param errors place to attach errors to.
     * @return true when the delete and copy was a success.
     * @throws MessagingException when JavaMail does
     */
    static boolean deleteMessage(final HttpServletRequest request, final Message message, final boolean deleteForever, final ActionErrors errors) throws MessagingException {
        final HttpSession session = request.getSession();
        final Folder folder = message.getFolder();
        Folder trashFolder = null;
        try {
            trashFolder = Util.getFolder(session, Constants.getTrashFolderFullname(session));
            if (!deleteForever && (trashFolder != null // if we are in the trash folder, don't bother
                    && !Constants.getTrashFolderFullname(session).equals(folder.getFullName()))) {
                // throw it in the trash
                logger.debug("copying message #" + message.getMessageNumber() + " to Trash");
                final List unfinishedList = ActionsUtil.copyMessages(new Message[]{message}, folder, trashFolder, errors);
                ActionsUtil.flushMailStoreGroupCache(request.getSession());
                if (unfinishedList.size() > 0) {
                    request.setAttribute(Constants.MESSAGE_LIST, unfinishedList);
                    request.setAttribute(Constants.DELETE_ACTION, "deleteMessage");
                    //return mapping.findForward("errorCopyToTrash");
                    return false;
                }
            }
        } finally {
            Util.releaseFolder(trashFolder); // clean up
        }
        message.setFlag(Flags.Flag.DELETED, true);
        folder.expunge();
        ActionsUtil.flushMailStoreGroupCache(session);
        return true;
    }
}
