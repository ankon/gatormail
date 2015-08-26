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
import edu.ufl.osg.webmail.forms.FolderForm;
import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.LookupDispatchAction;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;


/**
 * Do the actual deletion of a folder - this step comes after any required validation.
 *
 * @author drakee
 * @version $Revision: 1.3 $
 */
public class PerformDeleteFolderAction extends LookupDispatchAction {
    private static final Logger logger = Logger.getLogger(PerformDeleteFolderAction.class.getName());
    private Map map;

    public PerformDeleteFolderAction() {
        map = new HashMap();
        map.put("button.ok", "deleteFolder");
        map.put("button.cancel", "cancelDelete");
    }

    /**
     * Provides the mapping from resource key to method name
     *
     * @return                     Resource key / method name map
     */
    protected Map getKeyMethodMap() {
        return map;
    }

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
    public ActionForward cancelDelete(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        logger.debug("=== PerformDeleteFolderAction.cancelDelete() begin ===");
        ActionsUtil.checkSession(request);

        // success message for next page
        final ResultBean result = new ResultBean(Util.getFromBundle("performDeleteFolder.result.cancel"));
        request.setAttribute(Constants.RESULT, result);

        logger.debug("=== PerformDeleteFolderAction.cancelDelete() end ===");
        return mapping.findForward("cancel");
    }

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
    public ActionForward deleteFolder(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        logger.debug("=== PerformDeleteFolderAction.execute() begin ===");
        ActionsUtil.checkSession(request);
        final ActionErrors errors = new ActionErrors();

        final FolderForm folderForm = (FolderForm)form;
        final String folderFullName = folderForm.getFolder();
        final HttpSession session = request.getSession();
        final Folder folder = Util.getFolder(session, folderFullName);

        // Check that this folder qualifies for deletion.
        // This check was already done, but we want to guard against
        // people sending faked parameters to this action from the
        // confirm delete form.
        if (!ActionsUtil.isLegalDelete(folder, errors, session)) {
            saveErrors(request, errors);
            Util.releaseFolder(folder); // clean up
            return mapping.findForward("fail");
        }

        // folder must be closed for delete
        folder.close(true);

        try {
            folder.setSubscribed(false);
        } catch (MessagingException e) {
            // swallowed
        }

        // recursive delete is OK - if the user didn't check "deleteChildren" and there
        // were children, the form validation would have caught that.
        folder.delete(true);
        ActionsUtil.flushMailStoreGroupCache(session);

        // success message for next page
        final ResultBean result = new ResultBean(Util.getFromBundle("performDeleteFolder.result.success"));
        request.setAttribute(Constants.RESULT, result);

        Util.releaseFolder(folder); // clean up

        logger.debug("=== PerformDeleteFolderAction.execute() end ===");
        return mapping.findForward("success");
    }
}
