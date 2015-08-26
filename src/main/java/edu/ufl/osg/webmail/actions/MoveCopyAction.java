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
import edu.ufl.osg.webmail.forms.MoveCopyForm;
import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Moves or copies 1 or more messages into another folder.
 *
 * @author drakee
 * @version $Revision: 1.3 $
 */
public class MoveCopyAction extends Action {
    private static final Logger logger = Logger.getLogger(MoveCopyAction.class.getName());

    /**
     * Move or copy a message from one folder to another.
     *
     * @param     mapping             The ActionMapping used to select this
     *                                instance
     * @param     form                The optional ActionForm bean for this
     *                                request (if any)
     * @param     request             The HTTP request we are processing
     * @param     response            The HTTP response we are creating
     * @exception Exception if the application business
     *                                logic throws an exception
     */
    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        logger.debug("=== MoveCopyAction.execute() begin ===");
        ActionsUtil.checkSession(request);
        final HttpSession session = request.getSession();
        final ActionErrors errors = new ActionErrors();

        final MoveCopyForm moveCopyForm = (MoveCopyForm)form;
        final String action = moveCopyForm.getAction();
        final boolean isMove = (Util.getFromBundle("button.moveToFolder").equals(action) ? true : false);

        final Folder toFolder = Util.getFolder(session, moveCopyForm.getToFolder());
        final Folder folder = Util.getFolder(session, moveCopyForm.getFolder());

        final Message[] messages = ActionsUtil.fetchMessages(folder, moveCopyForm.getUid(), moveCopyForm.getMessageNumber());
        final List messageList = ActionsUtil.buildMessageList(messages);
        final int numMessages = messageList.size();
        if (numMessages == 0) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.messages.notexist"));
            saveErrors(request, errors);
            // clean up
            Util.releaseFolder(toFolder);
            Util.releaseFolder(folder);
            return mapping.findForward("folder");
        }

        // move messages into folder
        //folder.copyMessages(messages, toFolder);
        final List unfinishedList = ActionsUtil.copyMessages(messageList, folder, toFolder, errors);
        ActionsUtil.flushMailStoreGroupCache(request.getSession());
        if (unfinishedList.size() > 0) {
            request.setAttribute("folderName", toFolder.getFullName());
            request.setAttribute(Constants.MESSAGE_LIST, unfinishedList);
            saveErrors(request, errors);
            // clean up
            Util.releaseFolder(toFolder);
            Util.releaseFolder(folder);
            return mapping.findForward("errorCopy");
        }

        // success message for next page
        final ResultBean result = new ResultBean();
        if (isMove) {
            for (int i = 0; i < numMessages; i++) {
                // delete message from current folder
                ((Message)messageList.get(i)).setFlag(Flags.Flag.DELETED, true);
            }
            // make it so
            folder.expunge();
            ActionsUtil.flushMailStoreGroupCache(request.getSession());

            if (numMessages > 1) {
                result.setMessage(Util.getFromBundle("moveToFolder.result.multiple.success"), String.valueOf(numMessages), toFolder.getFullName());
            } else {
                result.setMessage(Util.getFromBundle("moveToFolder.result.single.success"), toFolder.getFullName());
            }
            logger.debug("moved " + numMessages + " messages.");
        } else { // copy action
            if (numMessages > 1) {
                result.setMessage(Util.getFromBundle("copyToFolder.result.multiple.success"), String.valueOf(numMessages), toFolder.getFullName());
            } else {
                result.setMessage(Util.getFromBundle("copyToFolder.result.single.success"), toFolder.getFullName());
            }
            logger.debug("copied " + numMessages + " messages.");
        }
        request.setAttribute(Constants.RESULT, result);

        // clean up
        Util.releaseFolder(toFolder);
        Util.releaseFolder(folder);

        logger.debug("=== MoveCopyAction.execute() end ===");
        return mapping.findForward("success");
    }
}
