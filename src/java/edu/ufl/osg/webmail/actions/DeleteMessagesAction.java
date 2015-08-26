/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002, 2003 William A. McArthur, Jr.
 * Copyright (C) 2003 The Open Systems Group / University of Florida
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
import edu.ufl.osg.webmail.forms.DeleteMessagesForm;
import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Deletes one or more messages selected from the DeleteMessagesForm.
 *
 * @author drakee
 * @author sandymac
 * @version $Revision: 1.4 $
 */
public class DeleteMessagesAction extends FolderAction {
    private static final Logger logger = Logger.getLogger(DeleteMessagesAction.class.getName());
    private static Comparator deleteMessageSort = new DeleteMessageSort();

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
        logger.debug("=== DeleteMessagesAction.execute() begin ===");
        ActionsUtil.checkSession(request);
        final ActionErrors errors = new ActionErrors();

        // get the current Folder
        Folder folder = null;
        try {
            folder = ActionsUtil.fetchFolder(form, request);
            request.setAttribute("folder", folder);
            final DeleteMessagesForm deleteMessagesForm = (DeleteMessagesForm)form;

            // get messages to delete
            final Message[] messages = ActionsUtil.fetchMessages(folder, deleteMessagesForm.getUid(), deleteMessagesForm.getMessageNumber());

            final List messageList = ActionsUtil.buildMessageList(messages);
            if (messageList.size() == 0) {
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.messages.notexist"));
                saveErrors(request, errors);
                return mapping.findForward("folder");
            }

            final boolean deleteForever = deleteMessagesForm.getDeleteForever();
            logger.debug("deleteForever: " + deleteForever);
            final int numMessages = messageList.size();

            Folder trashFolder = null;
            final HttpSession session = request.getSession();
            try {
                trashFolder = Util.getFolder(session, Constants.getTrashFolderFullname(session));
                if (!deleteForever && // if deleteForever is true, don't copy messages into trash
                        trashFolder != null && // if we are in the trash folder, don't bother
                        !Constants.getTrashFolderFullname(session).equals(folder.getFullName())) {

                    // sort messages from largest to smallest
                    Collections.sort(messageList, deleteMessageSort);

                    // throw it in the trash
                    logger.debug("copying " + numMessages + " messages to Trash");
                    final List unfinishedList = ActionsUtil.copyMessages(messageList, folder, trashFolder, errors);
                    ActionsUtil.flushMailStoreGroupCache(session);
                    if (unfinishedList.size() > 0) {
                        request.setAttribute(Constants.MESSAGE_LIST, unfinishedList);
                        request.setAttribute(Constants.DELETE_ACTION, "deleteMessages");
                        saveErrors(request, errors);
                        return mapping.findForward("errorCopyToTrash");
                    }
                }
            } finally {
                Util.releaseFolder(trashFolder); // clean up
            }

            // delete messages
            markMessagesForDelete(messageList);
            folder.expunge();
            ActionsUtil.flushMailStoreGroupCache(session);

            // success message for next page
            final ResultBean result = new ResultBean();
            if (deleteForever) {
                if (numMessages > 1) {
                    result.setMessage(Util.getFromBundle("deleteForever.result.multiple.success"), String.valueOf(numMessages));
                } else {
                    result.setMessage(Util.getFromBundle("deleteForever.result.single.success"));
                }
            } else {
                if (numMessages > 1) {
                    result.setMessage(Util.getFromBundle("delete.result.multiple.success"), String.valueOf(numMessages));
                } else {
                    result.setMessage(Util.getFromBundle("delete.result.single.success"));
                }
            }
            request.setAttribute(Constants.RESULT, result);

            logger.debug("=== DeleteMessagesAction.execute() end ===");
            return mapping.findForward("folder");
        } finally {
            Util.releaseFolder(folder); // clean up
        }
    }

    // marks all Messages in the list as DELETED
    private static void markMessagesForDelete(final List messageList) throws MessagingException {
        final int size = messageList.size();
        for (int i = 0; i < size; i++) {
            final Message message = ((Message)messageList.get(i));
            if (message != null && !message.isExpunged()) {
                message.setFlag(Flag.DELETED, true);
            }
        }
    }

    /**
     * Used to sort messages according to message size,
     * from largest to smallest.
     */
    private static class DeleteMessageSort implements Comparator {

        public int compare(final Object o1, final Object o2) {
            if ((o1 == null) || (o2 == null)) {
                throw new NullPointerException();
            }

            int size1 = 0;
            int size2 = 0;
            final Message m1 = (Message)o1;
            final Message m2 = (Message)o2;

            try {
                size1 = m1.getSize();
                size2 = m2.getSize();
            } catch (MessagingException me) {
                logger.error(me.toString());
            }

            return size2 - size1; // larger message first
        }
    }
}
