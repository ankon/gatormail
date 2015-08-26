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

import java.util.List;
import java.util.Set;

import javax.mail.Folder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ufl.osg.webmail.util.Util;

/**
 * Sets up the view for the folder manage page.
 *
 * @author drakee
 * @version $Revision: 1.4 $
 */
public class FolderManageAction extends Action {
    private static final Logger logger = LoggerFactory.getLogger(FolderManageAction.class);

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
        logger.debug("=== FolderManageAction.execute() begin ===");
        ActionsUtil.checkSession(request);
        final HttpSession session = request.getSession();

        // Get the current Folder
        final Folder folder = Util.getFolder(session, "INBOX");

        // Populate Quota info
        final Set quotaSet = ActionsUtil.getCachedQuotas(folder, session);
        request.setAttribute("quotaSet", quotaSet);

        Util.releaseFolder(folder);
        request.setAttribute("folder", folder);

        // Populate list for folder table - get all folders, even unsubscribed
        final List folderList = ActionsUtil.getFolders(Util.getMailStore(session));
        request.setAttribute("folderBeanList", folderList);

        logger.debug("=== FolderManageAction.execute() end ===");
        return mapping.findForward("success");
    }
}
