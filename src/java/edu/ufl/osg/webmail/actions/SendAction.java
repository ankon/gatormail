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

import com.sun.mail.imap.IMAPFolder;
import edu.ufl.osg.webmail.Constants;
import edu.ufl.osg.webmail.User;
import edu.ufl.osg.webmail.prefs.PreferencesProvider;
import edu.ufl.osg.webmail.beans.ResultBean;
import edu.ufl.osg.webmail.data.AttachDAOException;
import edu.ufl.osg.webmail.data.AttachList;
import edu.ufl.osg.webmail.data.AttachObj;
import edu.ufl.osg.webmail.forms.ComposeForm;
import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Flags;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Performs the sending of a composed message.
 *
 * @author sandymac
 * @author drakee
 * @version $Revision: 1.6 $
 */
public class SendAction extends Action {
    private static final Logger logger = Logger.getLogger(SendAction.class.getName());
    // start looking to wrap composed message body a little before the limit
    // RFC2646 suggests line length of 66 characters
    private static final int WRAP_WIDTH = 66; //Constants.COMPOSE_BODY_WIDTH - 4;

    /**
     * Sends an email.
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
        logger.debug("=== SendAction.execute() begin ===");
        ActionsUtil.checkSession(request);

        // validation
        final ActionErrors errors = new ActionErrors();
        final ComposeForm compForm = (ComposeForm)form;
        if (compForm.getTo() == null || compForm.getTo().trim().equals("")) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.compose.to.required"));
            saveErrors(request, errors);
            compForm.setAttachment(null);
            return mapping.findForward("fail");
        }

        final HttpSession httpSession = request.getSession();
        //         Session session = (Session)httpSession.getAttribute(Constants.MAIL_SESSION_KEY);
        final Session session = Util.getMailSession(httpSession);
        final String subject = compForm.getSubject();
        //String body = wrapLines(compForm.getBody());
        final String body = formatRfc2646(compForm.getBody());

        final InternetAddress[] to = parseAddresses(compForm.getTo(), errors);
        final InternetAddress[] cc = parseAddresses(compForm.getCc(), errors);
        final InternetAddress[] bcc = parseAddresses(compForm.getBcc(), errors);
        if (!errors.isEmpty()) {
            saveErrors(request, errors);
            compForm.setAttachment(null);
            return mapping.findForward("fail");
        }

        final MimeMessage message = new MimeMessage(session);
        final User user = Util.getUser(httpSession);
        message.setFrom(getFromAddress(user, httpSession));
        message.setRecipients(RecipientType.TO, to);
        message.setRecipients(RecipientType.CC, cc);
        message.setRecipients(RecipientType.BCC, bcc);
        message.setSubject(subject);
        message.setSentDate(new Date());
        message.setHeader("X-Mailer", "GatorMail WebMail (http://GatorMail.sf.net/)");
        message.setHeader("X-Originating-IP", request.getRemoteHost() + " [" + request.getRemoteAddr() + "]");

        final PreferencesProvider pp = (PreferencesProvider)getServlet().getServletContext().getAttribute(Constants.PREFERENCES_PROVIDER);

        final Properties prefs = pp.getPreferences(user, httpSession);
        final String replyTo = prefs.getProperty("compose.replyTo");
        if (replyTo != null) {
            final InternetAddress[] replyToAddr = parseAddresses(replyTo, errors);
            message.setReplyTo(replyToAddr);
        }

        final String imageUrl = prefs.getProperty("compose.X-Image-Url");
        if (imageUrl != null && imageUrl.length() > 0) {
            message.setHeader("X-Image-Url", imageUrl);
        }

        // get attachment list from memory
        final AttachList attachList = Util.getAttachList(compForm.getComposeKey(), httpSession);

        // create message content
        if (attachList.size() == 0) {
            logger.debug("no attachments");
            message.setContent(body, "text/plain; format=flowed");
            //message.addHeader("Content-Type", "text/plain; format=flowed");
        } else {
            logger.debug("there are attachments: " + attachList);
            final Multipart multipart = createMultipart(body, attachList, httpSession);
            // put all the parts into message
            message.setContent(multipart);
            message.saveChanges();
        }

        // do the sending
        Transport.send(message);
        logger.debug("successfully sent message");

        // is "copy to sent" checked?
        logger.debug("copyToSent value: " + compForm.isCopyToSent());
        if (compForm.isCopyToSent()) {
            logger.debug("copy message to Sent folder - start");
            final Folder sentFolder = Util.getFolder(httpSession, Constants.getSentFolderFullname(httpSession));
            message.setFlag(Flags.Flag.SEEN, true);
            ActionsUtil.flushMailStoreGroupCache(httpSession);
            if (!ActionsUtil.appendMessage(message, sentFolder, errors)) {
                // error - go to special error page
                saveErrors(request, errors);
                Util.releaseFolder(sentFolder); // clean up
                return mapping.findForward("errorCopyToSent");
            }
            Util.releaseFolder(sentFolder); // clean up
            logger.debug("copy message to Sent folder - end");
        }

        // success message for next page
        final ResultBean result = new ResultBean(Util.getFromBundle("send.result.success"));
        request.setAttribute(Constants.RESULT, result);

        // now that the message was sent successfully, get rid of attachments
        Util.removeAttachList(compForm.getComposeKey(), httpSession);

        logger.debug("=== SendAction.execute() end ===");
        return mapping.findForward("success");
    }

    // parses and validates a string of email addresses
    private static InternetAddress[] parseAddresses(final String addressLine, final ActionErrors errors) {
        InternetAddress[] addresses = null;
        try {
            addresses = InternetAddress.parse(addressLine);
            for (int i = 0; i < addresses.length; i++) {
                addresses[i].validate();
            }
        } catch (AddressException ae) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.address.invalid", addressLine));
        }
        return addresses;
    }

    // returns a MIME multipart - this is called when the email has attachments
    private static Multipart createMultipart(final String body, final AttachList attachList, final HttpSession session) throws MessagingException, AttachDAOException {
        final Multipart multipart = new MimeMultipart();

        // create the message part
        final BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body);
        multipart.addBodyPart(messageBodyPart);

        // attach any forwarded message
        for (int i = 0; i < attachList.size(); i++) {
            final AttachObj attachObj = (AttachObj)attachList.get(i);
            if (attachObj.getIsForward()) {
                final String folderFullName = attachObj.getFolderFullName();
                final Folder folder = Util.getFolder(session, folderFullName);
                // Find the message
                final Message message;
                if (attachObj.getUid() != null) {
                    // UID to get message
                    final IMAPFolder imapFolder = (IMAPFolder)folder;
                    message = imapFolder.getMessageByUID(attachObj.getUid().longValue());
                } else {
                    // messageNumber to get message
                    message = folder.getMessage(attachObj.getMessageNumber().intValue());
                }
                final BodyPart fwdBodyPart = new MimeBodyPart();
                fwdBodyPart.setDataHandler(message.getDataHandler());
                // Add part to multi part
                multipart.addBodyPart(fwdBodyPart);
                break;
            }
        }

        // now for the attachment(s)
        final List bodyPartList = attachList.getMimeBodyPartList();
        final int size = bodyPartList.size();
        for (int i = 0; i < size; i++) {
            final MimeBodyPart attachBodyPart = (MimeBodyPart)bodyPartList.get(i);
            multipart.addBodyPart(attachBodyPart);
            logger.debug("attached file: " + attachBodyPart.getFileName() + ", contentType: " + attachBodyPart.getContentType());
        }

        return multipart;
    }

    private InternetAddress getFromAddress(final User user, final HttpSession session) throws UnsupportedEncodingException {
        final PreferencesProvider pp = (PreferencesProvider)getServlet().getServletContext().getAttribute(Constants.PREFERENCES_PROVIDER);
        final Properties prefs = pp.getPreferences(user, session);

        return new InternetAddress(user.getEmail(), prefs.getProperty("user.name"));
    }

    /**
     * Formats a block of text so that it is <code>text/plain;
     * format=flowed</code> as described in RFC2646.
     *
     * @param  body                the block of text to wrap.
     * @return                     the rfc2646 form of <code>body</code>.
     * @throws IOException         Thrown from {@link
     *                             java.io.BufferedReader#readLine()}.
     */
    private static String formatRfc2646(final String body) throws IOException {
        final List lines = new ArrayList(body.length() / 40); // Guess
        final StringBuffer sb = new StringBuffer(body.length());
        final BufferedReader br = new BufferedReader(new StringReader(body));
        String line = null;

        // Build a list of lines.
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }

        // Rewrap lines
        for (int i = 0, size = lines.size(); i < size; i++) {
            line = (String)lines.get(i);
            final int quoteDepth = countQuoteDepth(line);
            line = line.substring(quoteDepth);

            // Rewrap quoted blocks
            if (quoteDepth != 0) {
                line = unStuff(line);

                // Join flowed lines to this one
                String nextLine = (i + 1 < lines.size()) ? (String)lines.get(i + 1) : null;
                while (nextLine != null && isFlowed(line) && countQuoteDepth(nextLine) == quoteDepth) {
                    nextLine = nextLine.substring(countQuoteDepth(nextLine));
                    line += unStuff(nextLine);
                    i++;
                    nextLine = (i + 1 < lines.size()) ? (String)lines.get(i + 1) : null;
                }
            }

            line = makeHardBreak(line);
            
            // Preserve cut lines
            if (line.equals("--")) {
                line += " ";
            }

            while (line != null) {
                if (line.length() < WRAP_WIDTH) {
                    // Line doesn't need wrapping
                    while (isFlowed(line)) {
                        line = line.substring(0, line.length() - 1);
                    }
                    sb.append(makeQuoteDepth(quoteDepth)).append(line).append("\r\n");
                    line = null;

                } else {
                    // Line needs to be wrapped
                    final String lineChunk;
                    int breakPoint = Math.min(line.length() - 1, WRAP_WIDTH - quoteDepth);
                    while (breakPoint > 1 && line.charAt(breakPoint) != ' ') {
                        breakPoint--;
                    }
                    if (1 == breakPoint) {
                        breakPoint = Math.min(line.length(), WRAP_WIDTH - quoteDepth);
                        while (breakPoint < line.length() && line.charAt(breakPoint) != ' ') {
                            breakPoint++;
                        }
                    }
                    if (line.length() > breakPoint + 1) {
                        breakPoint++;
                    }
                    lineChunk = line.substring(0, breakPoint);
                    line = line.substring(breakPoint);

                    sb.append(makeQuoteDepth(quoteDepth)).append(lineChunk).append("\r\n");

                    if (line.length() == 0) {
                        line = null;
                    }
                }
            }
        }

        return sb.toString();
    }

    private static String makeQuoteDepth(final int quoteDepth) {
        if (quoteDepth == 0) {
            return "";
        }

        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < quoteDepth; i++) {
            sb.append('>');
        }

        return sb.append(" ").toString();
    }

    private static boolean shouldStuff(final String line) {
        if (line.length() < 1) {
            return false;
        }

        final char c = line.charAt(0);
        return c == '>' || c == ' ';
    }

    private static String makeHardBreak(final String line) {
        final StringBuffer sb = new StringBuffer(line);
        while (sb.length() != 0 && sb.charAt(sb.length() - 1) == ' ') {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * Does this line end with a soft line break.
     *
     * @param  line                a {@link String} to test if ends with a soft
     *                             line break.
     * @return                     true if <code>line</code> ends with a soft
     *                             line break, else false.
     */
    private static boolean isFlowed(final String line) {
        return line.endsWith(" ");
    }

    private static String unStuff(final String line) {
        if (line.length() > 0 && line.charAt(0) == ' ') {
            return line.substring(1);
        }
        return line;
    }

    /**
     * Counts the number of '&gt;' at the start of a line.
     *
     * @param  line                a string
     * @return                     the number of '&gt;' at the start of a line
     */
    private static int countQuoteDepth(final String line) {
        final int length = line.length();
        int depth;

        for (depth = 0; depth < length && line.charAt(depth) == '>'; depth++) ;

        return depth;
    }

}
