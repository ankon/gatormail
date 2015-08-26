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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Message view controller.
 *
 * @author sandymac
 * @author drakee
 * @version $Revision: 1.3 $
 */
public class MessageAction extends FolderAction {
    private static final Logger logger = Logger.getLogger(MessageAction.class.getName());

    /**
     * Sets up the request enviroment for the message view. The current Folder
     * and Message are put in the request attributes.
     *
     * @param     mapping             The ActionMapping used to select this
     *                                instance
     * @param     form                The optional ActionForm bean for this
     *                                request (if any)
     * @param     request             The HTTP request we are processing
     * @param     response            The HTTP response we are creating
     * @return                        An ActionForward instance to either the
     *                                message view or a failure view.
     * @exception Exception if the application business
     *                                logic throws an exception
     */
    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        logger.debug("=== MessageAction.execute() begin ===");
        ActionsUtil.checkSession(request);
        final ActionErrors errors = new ActionErrors();
        
        // Get the current Sort
        final FolderForm folderForm = (FolderForm)form;
        request.setAttribute("folderSort", folderForm.getSort());

        // Open the folder
        final Folder folder = ActionsUtil.fetchFolder(form, request);
        request.setAttribute("folder", folder);

        // Find the message
        final Message message = ActionsUtil.fetchMessage(form, folder);
        if (message == null) {
            Util.releaseFolder(folder); // clean up
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.message.notexist"));
            saveErrors(request, errors);
            return mapping.findForward("folder");
        }
        request.setAttribute("message", message);

        // mark message as read
        message.setFlag(Flags.Flag.SEEN, true);
        Util.releaseFolder(folder); // clean up

        // Populate the folderBeanList
        final HttpSession session = request.getSession();
        final List folderList = ActionsUtil.getSusbscribedFolders(Util.getMailStore(session));
        request.setAttribute("folderBeanList", folderList);

        logger.debug("=== MessageAction.execute() end ===");
        return mapping.findForward("success");
    }
}
