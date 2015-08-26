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

import edu.ufl.osg.webmail.Constants;
import edu.ufl.osg.webmail.beans.ResultBean;
import edu.ufl.osg.webmail.forms.NewFolderForm;
import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.mail.Folder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Renames a Folder.
 *
 * @author drakee
 * @version $Revision: 1.6 $
 */
public class RenameFolderAction extends Action {
    private static final Logger logger = Logger.getLogger(RenameFolderAction.class.getName());

    /**
     *
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
        logger.debug("=== RenameFolderAction.execute() begin ===");
        ActionsUtil.checkSession(request);
        final ActionErrors errors = new ActionErrors();

        final HttpSession session = request.getSession();
        final NewFolderForm newFolderForm = (NewFolderForm)form;
        final String oldFolderName = newFolderForm.getFolder();  // folder to be renamed
        final String renameTo = newFolderForm.getNewFolder();     // new folder name
        logger.debug("oldFolderName: " + oldFolderName + ", renameTo: " + renameTo);

        final Folder oldFolder = Util.getFolder(session, oldFolderName);
        // check if folder is reserved, i.e. not allowed to be renamed
        if (Util.isReservedFolder(oldFolderName, session)) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.renameFolder.reserved"));
            saveErrors(request, errors);
            return mapping.findForward("fail");
        }

        final boolean wasSubscribed = oldFolder.isSubscribed();

        final Folder parentFolder = oldFolder.getParent();
        final String parentFolderName = parentFolder.getFullName();
        if (!ActionsUtil.isLegalNewFolder(renameTo, parentFolderName, errors, request)) {
            saveErrors(request, errors);
            return mapping.findForward("fail");
        }

        // Folder.renameTo() only works on closed folders
        if (oldFolder.isOpen()) {
            oldFolder.close(false);
        }
        // OK looks good. let's rename that folder
        final Folder newFolder = parentFolder.getFolder(renameTo);
        oldFolder.renameTo(newFolder);

        newFolder.setSubscribed(wasSubscribed);
        ActionsUtil.flushMailStoreGroupCache(session);

        // reset renameTo field
        newFolderForm.reset(mapping, request);

        // clean up
        Util.releaseFolder(oldFolder);
        Util.releaseFolder(newFolder);

        // set success message for the upcoming view
        final ResultBean result = new ResultBean(Util.getFromBundle("renameFolder.result.success"));
        request.setAttribute(Constants.RESULT, result);

        logger.debug("=== RenameFolderAction.execute() end ===");
        return mapping.findForward("success");
    }
}
