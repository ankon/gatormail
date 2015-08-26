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
import edu.ufl.osg.webmail.forms.FolderForm;
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

/**
 * Subscribes and unsubscribes a folder.
 *
 * @author drakee
 * @version $Revision: 1.5 $
 */
public class ChangeSubscribedAction extends Action {
    private static final Logger logger = Logger.getLogger(ChangeSubscribedAction.class.getName());

    /**
     * Makes folder subscribed.
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
        logger.debug("=== ChangeSubscribedAction.subscribeFolder() begin ...");

        final FolderForm folderForm = (FolderForm)form;
        final String subscribeFolder = folderForm.getFolder();

        Folder folder = null;
        try {
            folder = Util.getFolder(request.getSession(), subscribeFolder);
            logger.debug("subscribeFolder: " + folder);

            if (folder.isSubscribed()) {

                if (folder.getFullName().equals("INBOX")) {

                    final ActionErrors errors = new ActionErrors();
                    errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.changeSubscription.unsubscribe.inbox"));
                    saveErrors(request, errors);
                    return mapping.findForward("fail");
                }

                // make folder subscribed or unsubscribed
                folder.setSubscribed(false);

                request.setAttribute(Constants.RESULT, new ResultBean(Util.getFromBundle("changeSubscription.result.unsubscribed.success")));
                logger.debug("=== ChangeSubscribedAction.unsubscribeFolder() end ===");
            } else {
                folder.setSubscribed(true);
                // set success message for the upcoming view
                request.setAttribute(Constants.RESULT, new ResultBean(Util.getFromBundle("changeSubscription.result.subscribed.success")));
                logger.debug("=== ChangeSubscribedAction.subscribeFolder() end ===");
            }
            ActionsUtil.flushMailStoreGroupCache(request.getSession());

        } finally {
            Util.releaseFolder(folder); // clean up
        }
        return mapping.findForward("success");
    }
}
