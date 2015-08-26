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
import edu.ufl.osg.webmail.data.AttachList;
import edu.ufl.osg.webmail.data.AttachObj;
import edu.ufl.osg.webmail.data.ConfigDAO;
import edu.ufl.osg.webmail.data.DAOFactory;
import edu.ufl.osg.webmail.formatters.SizeFormat;
import edu.ufl.osg.webmail.forms.ComposeForm;
import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.LookupDispatchAction;
import org.apache.struts.upload.FormFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles user requests for sending mail, uploading and deleting attachments
 * in the compose view.
 *
 * @author drakee
 * @version $Revision: 1.4 $
 */
public class ModifyComposeAction extends LookupDispatchAction {
    private static final Logger logger = Logger.getLogger(ModifyComposeAction.class.getName());
    private Map map = new HashMap();

    public ModifyComposeAction() {
        map.put("button.send", "sendMessage");
        map.put("button.attachment.upload", "uploadAttachment");
        map.put("button.attachment.delete", "deleteAttachment");
        map.put("button.cancelMessage", "cancelMessage");
    }

    protected Map getKeyMethodMap() {
        return map;
    }

    /**
     * Send an email
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
    public ActionForward sendMessage(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        logger.debug("=== ModifyComposeAction.sendMessage() begin ===");
        ActionsUtil.checkSession(request);

        final ComposeForm compForm = (ComposeForm)form;
        // deal with any attachment that may be in the "upload" box
        final FormFile attachment = compForm.getAttachment();
        if (attachment != null) {
            final String fileName = attachment.getFileName();
            logger.debug("attachment fileName: " + fileName);
            // if setting the attachList fails...
            if (fileName != null && !fileName.trim().equals("") && !setAttachList(compForm.getComposeKey(), attachment, request)) {
                // setAttachList(..) didn't work - go to fail forward
                logger.error("setAttachList failed");
                //compForm.setAttachment(null);
                return mapping.findForward("fail");
            }
        }
        //compForm.setAttachment(null);

        logger.debug("=== ModifyComposeAction.sendMessage() end ===");
        return mapping.findForward("send");
    }

    public ActionForward cancelMessage(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        logger.debug("=== ModifyComposeAction.cancelMessage() begin ===");
        ActionsUtil.checkSession(request);

        final ActionErrors errors = new ActionErrors();
        final HttpSession session = request.getSession();
        final ComposeForm compForm = (ComposeForm)form;

        // get attachments List from session
        final AttachList attachList = Util.getAttachList(compForm.getComposeKey(), session);

        attachList.clear();

        logger.debug("=== ModifyComposeAction.cancelMessage() end ===");
        return mapping.findForward("inbox");
    }

    /**
     * Deletes selected attachments.
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
    public ActionForward deleteAttachment(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        logger.debug("=== DeleteAttachmentAction.execute() begin ===");
        ActionsUtil.checkSession(request);

        final ActionErrors errors = new ActionErrors();
        final HttpSession session = request.getSession();
        final ComposeForm compForm = (ComposeForm)form;

        // get attachments List from session
        final AttachList attachList = Util.getAttachList(compForm.getComposeKey(), session);

        // get request parameter values
        final String[] delAttachments = request.getParameterValues(Constants.DELETE_ATTACHMENT);
        // if user didn't select any attachments to delete
        if (delAttachments == null || delAttachments.length == 0) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.attachment.required"));
            saveErrors(request, errors);
            return mapping.findForward("fail");
        }

        // go through parameters to see what user selected for deletion.
        for (int i = 0; i < delAttachments.length; i++) {
            logger.debug("going to remove " + delAttachments[i] + " from attachments");

            // remove the attachment from session-scoped list
            final boolean exitVal = attachList.removeByTempName(delAttachments[i]);
            if (!exitVal) {
                errors.add("deleteAttachment", new ActionError("error.attachment.delete.fail"));
                saveErrors(request, errors);
                return mapping.findForward("fail");
            }
        }
        compForm.setAttachment(null); // cleanup

        logger.debug("=== DeleteAttachmentAction.execute() end ===");
        return mapping.findForward("success");
    }

    /**
     * Upload an attachment
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
    public ActionForward uploadAttachment(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        logger.debug("=== ModifyComposeAction.uploadAttachment() begin ===");
        ActionsUtil.checkSession(request);

        final ComposeForm compForm = (ComposeForm)form;
        final FormFile attachment = compForm.getAttachment();
        logger.debug("compForm.attachment: " + attachment);
        if (!setAttachList(compForm.getComposeKey(), attachment, request)) {
            compForm.setAttachment(null);
            return mapping.findForward("fail");
        }
        compForm.setAttachment(null);
        logger.debug("=== ModifyComposeAction.uploadAttachment() ===");
        return mapping.findForward("success");
    }

    /**
     * Call this when user hits the "attach file" button.
     *
     * @return                     true if attachment was successfully added,
     *                             false if error.
     */
    private boolean setAttachList(final String composeKey, final FormFile attachment, final HttpServletRequest request) throws Exception {
        logger.debug("start of setAttachList(). attachment: " + attachment);
        // check that upload was successful
        if (attachment.getFileSize() == 0) {
            logger.warn("uploaded attachment has file size 0: " + attachment.getFileName());
            doAttachError(new ActionError("error.attachment.empty", attachment.getFileName()), request);
            return false;
        }
        // retrieve attachment list for this user
        final HttpSession session = request.getSession();
        final AttachList attachList = Util.getAttachList(composeKey, session);
        // check that the new upload doesn't have a duplicate name
        if (attachList.containsFileName(attachment.getFileName())) {
            logger.warn("duplicate attachment name: " + attachment.getFileName());
            doAttachError(new ActionError("error.attachment.duplicate", attachment.getFileName()), request);
            return false;
        }

        // check that combined attachment size isn't too large
        int attachSize = attachment.getFileSize();
        logger.debug("this attachment's size: " + attachSize);
        attachSize += attachList.getCombinedSize();
        logger.debug("total size of attachments: " + attachSize);
        final ConfigDAO configDAO = DAOFactory.getInstance().getConfigDAO();
        final String maxsize = configDAO.getProperty("maxsizeAttachments");
        if (attachSize > Integer.parseInt(maxsize)) {
            logger.warn("attachment(s) size too large: " + attachSize);
            final SizeFormat sizeFormat = new SizeFormat(Util.getFromBundle("general.size.format"));
            doAttachError(new ActionError("error.attachment.toolarge", sizeFormat.format(new Integer(maxsize), new StringBuffer(), null).toString()), request);
            return false;
        }

        // find unused, non-null name for this file
        final String fileName = attachList.generateFileName(attachment.getFileName(), attachment.getContentType());
        final AttachObj attachObj = new AttachObj();
        attachObj.setFileName(fileName);
        attachObj.setContentType(attachment.getContentType());
        attachObj.setSize(attachment.getFileSize());

        // add attachment to session-scoped attachList & storage backend
        attachList.addAttachment(attachObj, attachment.getFileData());
        logger.debug("added new attachment: " + attachList);
        return true;
    }

    private void doAttachError(final ActionError actionError, final HttpServletRequest request) {
        final ActionErrors errors = new ActionErrors();
        errors.add(ActionErrors.GLOBAL_ERROR, actionError);
        saveErrors(request, errors);
    }
}
