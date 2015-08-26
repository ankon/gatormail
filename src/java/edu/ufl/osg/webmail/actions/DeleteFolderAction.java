/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002-2004 William A. McArthur, Jr.
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

import edu.ufl.osg.webmail.forms.FolderForm;
import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.mail.Folder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Deletes a Folder.
 *
 * @author drakee
 * @author sandymac
 * @version $Revision: 1.5 $
 */
public class DeleteFolderAction extends Action {
    private static final Logger logger = Logger.getLogger(DeleteFolderAction.class.getName());

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
        logger.debug("=== DeleteFolderAction.execute() begin ===");
        ActionsUtil.checkSession(request);
        final ActionErrors errors = new ActionErrors();

        final FolderForm folderForm = (FolderForm)form;
        final String deleteFolder = folderForm.getFolder();

        Folder folder = null;
        try {
            final HttpSession session = request.getSession();
            folder = Util.getFolder(session, deleteFolder);
            logger.debug("deleteFolder: " + folder);

            // check that this folder qualifies for deletion
            if (!ActionsUtil.isLegalDelete(folder, errors, session)) {
                logger.error("illegal delete: " + folder.getFullName());
                saveErrors(request, errors);
                return mapping.findForward("fail");
            }
            request.setAttribute("folder", folder);
        } finally {
            Util.releaseFolder(folder); // clean up
        }

        final List folderList = ActionsUtil.getFolders(folder);
        request.setAttribute("affectedFolderList", folderList);

        logger.debug("=== DeleteFolderAction.execute() end ===");
        return mapping.findForward("success");
    }
}
