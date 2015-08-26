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

import edu.ufl.osg.webmail.User;
import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;

/**
 * Reports a message to the false positve address.
 *
 * @author laura2
 * @since Sep 26, 2005
 * @version $Revision: 1.2 $
 */
public class ReportHamAction extends MessageAction {
    private static final Logger logger = Logger.getLogger(ReportHamAction.class.getName());
    private static final String FWD = "Fwd: ";

    /**
     * Forwards the email to report-ham.ufl.edu
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
        logger.debug("=== ReportHamAction.execute() begin ===");

        ActionsUtil.checkSession(request);
        final HttpSession session = request.getSession();
        final ActionErrors errors = new ActionErrors();

        // open the folder
        final Folder folder = ActionsUtil.fetchFolder(form, request);
        request.setAttribute("folder", folder);

        // find the originalMessage
        final Message originalMessage = ActionsUtil.fetchMessage(form, folder);

        if (originalMessage == null) {
            Util.releaseFolder(folder); // clean up
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.originalMessage.notexist"));
            saveErrors(request, errors);
            return mapping.findForward("folder");
        }

        // TODO: Forward a copy to report-ham
        final Message messageWrapper = new MimeMessage(Util.getMailSession(session));

        // Fill in header
        String subject = FWD + originalMessage.getSubject();
        final String[] spamStatusHeader = originalMessage.getHeader("X-UFL-Spam-Status");
        if (spamStatusHeader != null) {
            subject += " " + Arrays.asList(spamStatusHeader);
        }
        messageWrapper.setSubject(subject);

        final User user = Util.getUser(session);
        messageWrapper.setFrom(new InternetAddress(user.getEmail()));
        final String hamAddress = servlet.getServletContext().getInitParameter("report.ham.email");
        if (hamAddress == null || hamAddress.length() == 0) {
            throw new RuntimeException("report.ham.email context parameter missing");
        }
        final Address[] toHam = InternetAddress.parse(hamAddress);
        messageWrapper.setRecipients(Message.RecipientType.TO, toHam);

        // Create a multi-part to combine the parts
        final Multipart multipart = new MimeMultipart();

        // Create and fill part for the forwarded content
        final MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(originalMessage, "message/rfc822");

        // Add part to multi part
        multipart.addBodyPart(messageBodyPart);

        // Associate multi-part with message
        messageWrapper.setContent(multipart);

        // do the sending
        Transport.send(messageWrapper);
        logger.debug("successfully sent originalMessage");

        Util.releaseFolder(folder); // clean up
        logger.debug("=== ReportHamAction.execute() end ===");
        final ActionForward forward = mapping.findForward("inbox");
        return forward; // TODO Fix so it goes back to correct folder
    }
}
