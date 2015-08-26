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

import edu.ufl.osg.webmail.User;
import edu.ufl.osg.webmail.Constants;
import edu.ufl.osg.webmail.prefs.PreferencesProvider;
import edu.ufl.osg.webmail.data.AttachList;
import edu.ufl.osg.webmail.data.AttachObj;
import edu.ufl.osg.webmail.forms.ComposeForm;
import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.mail.Folder;
import javax.mail.Message;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * Sets up the compose view for forwarding a message.
 *
 * @author drakee
 * @version $Revision: 1.2 $
 */
public class ForwardAction extends MessageAction {
    private static final Logger logger = Logger.getLogger(ForwardAction.class.getName());
    private static final String FWD = "Fwd: ";
    private static final String FWD_CAPS = FWD.toUpperCase();

    /**
     * Forwards an email
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
        logger.debug("=== ForwardAction.execute() begin ===");
        ActionsUtil.checkSession(request);
        final ActionErrors errors = new ActionErrors();

        // open the folder
        final Folder folder = ActionsUtil.fetchFolder(form, request);
        request.setAttribute("folder", folder);

        // find the message
        final Message message = ActionsUtil.fetchMessage(form, folder);
        if (message == null) {
            Util.releaseFolder(folder); // clean up
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.message.notexist"));
            saveErrors(request, errors);
            return mapping.findForward("folder");
        }
        request.setAttribute("message", message);

        final ComposeForm compForm = (ComposeForm)form;
        // puts Fwd: in front of subject
        compForm.setSubject(makeForwardSubject(message.getSubject()));

        // signature
        final HttpSession session = request.getSession();
        final User user = Util.getUser(session);
        final PreferencesProvider pp = (PreferencesProvider)getServlet().getServletContext().getAttribute(Constants.PREFERENCES_PROVIDER);
        compForm.setBody("\r\n\r\n\r\n" + pp.getPreferences(user, session).getProperty("compose.signature"));


        final String composeKey = Util.generateComposeKey(session);
        logger.debug("generated composeKey: " + composeKey);
        compForm.setComposeKey(composeKey);

        // Default CopyToSent to true.
        compForm.setCopyToSent(true);

        // handle the message to forward
        final Long uid = compForm.getUid();
        final Integer messageNumber = compForm.getMessageNumber();
        final AttachList attachList = Util.getAttachList(composeKey, session);
        attachList.add(new AttachObj(message, uid, messageNumber));
        Util.releaseFolder(folder); // clean up

        logger.debug("=== ForwardAction.execute() end ===");
        return mapping.findForward("success");
    }

    private static String makeForwardSubject(final String subject) {
        if (subject == null || subject.length() < 1)
            return FWD;
        return (!subject.toUpperCase().startsWith(FWD_CAPS) ? FWD + subject : subject);
    }
}
