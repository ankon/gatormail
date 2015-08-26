/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002-2004 William A. McArthur, Jr.
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
import edu.ufl.osg.webmail.forms.ComposeForm;
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
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Sets up the compose view for replying to a message.
 *
 * @author drakee
 * @author sandymac
 * @version $Revision: 1.3 $
 */
public class ReplyAction extends MessageAction {
    private static final Logger logger = Logger.getLogger(ReplyAction.class.getName());

    /**
     * Composes an email
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
        logger.debug("=== ReplyAction.execute() begin ===");
        ActionsUtil.checkSession(request);
        final ActionErrors errors = new ActionErrors();

        // open the folder
        final Folder folder = ActionsUtil.fetchFolder(form, request);
        request.setAttribute("folder", folder);

        // find the message
        final MimeMessage message = (MimeMessage)ActionsUtil.fetchMessage(form, folder);
        if (message == null) {
            Util.releaseFolder(folder); // clean up
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.message.notexist"));
            saveErrors(request, errors);
            return mapping.findForward("folder");
        }
        request.setAttribute("message", message);

        final ComposeForm compForm = (ComposeForm)form;
        final String action = compForm.getAction();
        final Message replyMessage = "reply-all".equals(action) ? message.reply(true) : message.reply(false);
        // to == the reply-to address(es)
        compForm.setTo(ActionsUtil.getAddressString(replyMessage.getRecipients(Message.RecipientType.TO)));
        // cc == same as original message
        compForm.setCc(ActionsUtil.getAddressString(replyMessage.getRecipients(Message.RecipientType.CC)));
        // bcc == same as original message
        compForm.setBcc(ActionsUtil.getAddressString(replyMessage.getRecipients(Message.RecipientType.BCC)));
        // puts an Re: in front of subject
        compForm.setSubject(replyMessage.getSubject());
        // set content:
        final StringBuffer textBuff = new StringBuffer();

        // add signature to blank message body
        final HttpSession session = request.getSession();
        final User user = Util.getUser(session);
        final PreferencesProvider pp = (PreferencesProvider)getServlet().getServletContext().getAttribute(Constants.PREFERENCES_PROVIDER);
        compForm.setBody("\r\n\r\n\r\n" + pp.getPreferences(user, session).getProperty("compose.signature"));

        // inline text messages
        final ReplyContent rc = new ReplyContent(message);
        final String attachText = rc.getText();
        if (attachText.length() > 0) {
            textBuff.append("\n\n" + wrapInlineQuote(message, attachText));
        }
        compForm.setBody(textBuff.toString() + compForm.getBody());

        Util.releaseFolder(folder); // clean up

        // new compose key for this "compose session"
        compForm.setComposeKey(Util.generateComposeKey(session));

        // Default CopyToSent to true.
        compForm.setCopyToSent(true);

        logger.debug("=== ReplyAction.execute() end ===");
        return mapping.findForward("success");
    }

    private static String wrapInlineQuote(final Message message, final String content) throws MessagingException, IOException {
        final String QUOTE_STRING = ">";
        final Address[] fromAddresses = message.getFrom();
        // TODO: This needs to be I18N-ized
        final StringBuffer buff = new StringBuffer("On " + message.getSentDate() + ", " + ActionsUtil.getAddressString(fromAddresses) + " wrote:\n\n");

        final BufferedReader br = new BufferedReader(new StringReader(content));
        String line = null;
        while ((line = br.readLine()) != null) {
            if (line.startsWith(QUOTE_STRING)) {
                buff.append(QUOTE_STRING + line + "\n");
            } else {
                buff.append(QUOTE_STRING + " " + line + "\n");
            }
        }
        return buff.toString();
    }

    /**
     * This is the Container for temporarily holding MimeMessage text content.
     * (useful for inlining replies when the getText() method is called).
     *
     * @author drakee
     */
    private static class ReplyContent {
        private List contentList;

        // constructor
        public ReplyContent(final MimeMessage message) throws IOException, MessagingException {
            contentList = new ArrayList();

            if (message.isMimeType("multipart/*")) {
                digMimeMultipart((MimeMultipart)message.getContent());
            } else { // single attachment
                if (isTextPart(message)) {
                    contentList.add(message.getContent());
                }
            }
        }

        /**
         * Return only the text content of the message.
         */
        public String getText() {
            final StringBuffer buff = new StringBuffer();
            final int size = contentList.size();
            for (int i = 0; i < size; i++) {
                buff.append((String)contentList.get(i) + "\n");
                // if this isn't the last item, add a newline
                // (with space so it doesn't later get parsed out).
                if (i < size - 1) {
                    buff.append(" \n");
                }
            }
            return buff.toString();
        }

        /**
         * Recursively drill down into a MimeMultipart and find text content.
         */
        private void digMimeMultipart(final MimeMultipart multipart) throws IOException, MessagingException {
            final int count = multipart.getCount();
            for (int i = 0; i < count; i++) {
                final MimeBodyPart bodyPart = (MimeBodyPart)multipart.getBodyPart(i);
                if (bodyPart.isMimeType("multipart/*")) {
                    // keep up the recursive digging
                    digMimeMultipart((MimeMultipart)bodyPart.getContent());
                } else { // bodyPart is a single part
                    if (isTextPart(bodyPart)) {
                        contentList.add(bodyPart.getContent());
                    }
                }
            }
        }

        /**
         * Tests whether or not a Part is of type "text/plain"
         */
        private boolean isTextPart(final Part part) throws MessagingException {
            final String contentType = part.getContentType();
            return contentType != null && contentType.toLowerCase().startsWith("text/plain");
        }
    }

}
