/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002, 2003 William A. McArthur, Jr.
 * Copyright (C) 2003-2005 The Open Systems Group / University of Florida
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
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.mail.Folder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Set;

/**
 * Sets up the view for the folder manage modify page.
 *
 * @author drakee
 * @version $Revision: 1.5 $
 */
public class FolderManageModifyAction extends Action {
    private static final Logger logger = Logger.getLogger(FolderManageModifyAction.class.getName());

    /**
     * Sets up the request for folder options view.
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
        logger.debug("=== FolderManageModifyAction.execute() begin ===");
        ActionsUtil.checkSession(request);
        final HttpSession session = request.getSession();
        final FolderForm folderForm = (FolderForm)form;

        // Get the requested Folder
       final Folder folder = Util.getFolder(session, folderForm.getFolder());

        // Populate Quota info
        final Set quotaSet = ActionsUtil.getCachedQuotas(folder, session);
        request.setAttribute("quotaSet", quotaSet);

        // Populate folder subscribed status
        final boolean isSubscribed = folder.isSubscribed();
        request.setAttribute("isSubscribed", String.valueOf(isSubscribed));

        Util.releaseFolder(folder);
        request.setAttribute("folder", folder);

        logger.debug("=== FolderManageModifyAction.execute() end ===");
        return mapping.findForward("success");
    }
}
