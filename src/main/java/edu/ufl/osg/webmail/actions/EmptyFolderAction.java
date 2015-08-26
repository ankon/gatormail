/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2005 The Open Systems Group / University of Florida
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
import edu.ufl.osg.webmail.forms.EmptyFolderForm;
import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.LookupDispatchAction;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deletes messages in a selected folder, warns user that all messages
 * in the folder are about to be deleted and prompts for confirmation or
 * cancelation of the delete action. Deletion of messages in the folder can be
 * permanent or by moving the messages to the trash folder.
 *
 * @author laura2
 * @version $Revision: 1.2 $
 * @since Oct 7, 2005
 */
public class EmptyFolderAction extends LookupDispatchAction {
    private static final Logger logger = Logger.getLogger(EmptyFolderAction.class.getName());
    private Map map;
    Folder trashFolder = null;

    public EmptyFolderAction() {
        map = new HashMap();
        map.put("button.emptyTrash", "emptyFolder");
        map.put("button.emptyFolder", "emptyFolder");
        map.put("button.confirmEmptyFolder", "confirmOk");
        map.put("button.cancelEmptyFolder", "confirmCancel");
    }

    /**
     * Provides the mapping from resource key to method name
     *
     * @return Resource key / method name map
     */
    protected Map getKeyMethodMap() {
        return map;
    }

    public ActionForward emptyFolder(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final EmptyFolderForm emptyForm = (EmptyFolderForm)form;
        if ("true".equals(emptyForm.getSure())){
            if("true".equals(emptyForm.getPermanent())){
                return actuallyDeleteMessages(mapping, form, request, response);
            } else {
                return moveToTrash(mapping, form, request, response);
            }

        } else {
            final HttpSession session = request.getSession();
            final String folderName = emptyForm.getFolderName();
            final Folder folder = Util.getFolder(session, folderName);
            Util.releaseFolder(folder);
            request.setAttribute("folder", folder);
            request.setAttribute("folderName", folderName);
            request.setAttribute("perm", emptyForm.getPermanent());
            return mapping.findForward("confirmEmptyFolder");
        }
    }

    public ActionForward confirmOk(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return emptyFolder(mapping, form, request, response);
    }

    public ActionForward confirmCancel(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return mapping.findForward("inbox");
    }
   /**
     * Used to move messages to trash folder
     */
    public ActionForward moveToTrash(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        logger.debug("=== EmptyFolderAction.moveToTrash() begin ===");
        final EmptyFolderForm emptyForm = (EmptyFolderForm)form;
        final HttpSession session = request.getSession();
        final ActionErrors errors = new ActionErrors();
        final Folder folder = Util.getFolder(session, emptyForm.getFolderName());
        final Folder toFolder = Util.getFolder(session, Constants.getTrashFolderFullname(session));

        final Message[] messages =  folder.getMessages();
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
        folder.copyMessages(messages, toFolder);
        for (int i = 0; i < numMessages; i++) {
            final Message message = ((Message)messageList.get(i));
            if (message != null && !message.isExpunged()) {
                message.setFlag(Flags.Flag.DELETED, true);
            }
        }

        // empty folder
        folder.expunge();
        ActionsUtil.flushMailStoreGroupCache(session);
        logger.debug("folder expunged.");
        request.setAttribute("folder", folder);

        // clean up
        Util.releaseFolder(toFolder);
        Util.releaseFolder(folder);

        logger.debug("=== EmptyFolderAction.moveToTrash() end ===");
        return mapping.findForward("inbox");
    }

    /**
     * Used to delete the messages immediately.
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
    public ActionForward actuallyDeleteMessages(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        logger.debug("=== EmptyFolderAction.actuallyDeleteMessages() begin ===");
        ActionsUtil.checkSession(request);
        final HttpSession session = request.getSession();
        final EmptyFolderForm emptyForm = (EmptyFolderForm)form;
        final String folderName = emptyForm.getFolderName();
        Folder folder = null;
        try {
            folder = Util.getFolder(session, folderName);
            if (folder != null) {
                final Message[] messages = folder.getMessages();
                // make sure all items in folder are marked for delete
                for (int i = 0; i < messages.length; i++) {
                    messages[i].setFlag(Flags.Flag.DELETED, true);
                    logger.debug("marked message " + i + " for deletion.");
                }
                // empty folder
                folder.expunge();
                ActionsUtil.flushMailStoreGroupCache(session);
                logger.debug("folder expunged.");
                request.setAttribute("folder", folder);
            }
            Util.releaseFolder(folder);
            logger.debug("=== EmptyFolderAction.actuallyDeleteMessages() end ===");
            ActionsUtil.flushMailStoreGroupCache(session);
            return mapping.findForward("inbox");
        } finally {
            Util.releaseFolder(folder);
        }
    }
}