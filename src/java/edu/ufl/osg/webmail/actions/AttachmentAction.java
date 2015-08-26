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

import edu.ufl.osg.webmail.InvalidSessionException;
import edu.ufl.osg.webmail.forms.AttachmentForm;
import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentDisposition;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.util.StringTokenizer;


/**
 * Controller that outputs part of a Message for use as an inline image or
 * downloading an attachment. See <a
 * href="ftp://ftp.isi.edu/in-notes/rfc2616.txt">rfc 2616</a> for more info.
 *
 * @author sandymac
 * @version $Revision: 1.4 $
 */
public class AttachmentAction extends MessageAction {
    private static final Logger logger = Logger.getLogger(AttachmentAction.class.getName());

    /**
     * Sends the requested part of the message to the user.
     *
     * @param  mapping                      The ActionMapping used to select
     *                                      this instance.
     * @param  form                         The optional ActionForm bean for
     *                                      this request (if any).
     * @param  request                      The HTTP request we are processing.
     * @param  response                     The HTTP response we are creating.
     * @return                              <code>null</code> because this
     *                                      action takes care of sending data.
     * @throws IOException
     * @throws MessagingException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvalidSessionException
     * @throws UnsupportedEncodingException
     * @throws SocketException              Where there is a problem writing to
     *                                      the response. Usually when the users
     *                                      presses the stop button.
     */
    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws  MessagingException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InvalidSessionException, IOException, SocketException, UnsupportedEncodingException {
        ActionsUtil.checkSession(request);

        Folder folder = null;
        final AttachmentForm attachmentForm = (AttachmentForm)form;
        try {
            // Open the folder
            folder = ActionsUtil.fetchFolder(form, request);
            request.setAttribute("folder", folder);

            // Find the message
            final Message message = ActionsUtil.fetchMessage(form, folder);

            // Get the Part
            final Part part = fetchBodyPart(attachmentForm, message, request);

            // Set headers
            final ContentType contentType = new ContentType(part.getContentType());
            response.setContentType(contentType.getBaseType());

            // XXX: More of them

            final ContentDisposition contentDisposition = new ContentDisposition("attachment");
            String fileName = part.getFileName();
            if (fileName != null) {
                if (fileName.startsWith("=?")) {
                    fileName = MimeUtility.decodeWord(fileName);
                }
                contentDisposition.setParameter("filename", fileName);
            }

            // This is a dumb ass hack because WebSphere 4 doesn't implement
            // HTTP/1.0 and HTTP/1.1 headers correctly as defined in section 2.4.
            final boolean dumbAssWebAppContainer = response.getOutputStream().getClass().getName().equals("com.ibm.servlet.engine.srt.BufferedServletOutputStream");
            String contentDispositionString = contentDisposition.toString();
            if (dumbAssWebAppContainer) {
                final StringBuffer sb = new StringBuffer(contentDispositionString.length());
                for (int i = 0, sLen = contentDispositionString.length(); i < sLen; i++) {
                    final char c = contentDispositionString.charAt(i);
                    switch (c) {
                        case '\n':
                        case '\r':
                            // Do nothing
                            break;
                        default:
                            sb.append(c);
                            break;
                    }
                }
                contentDispositionString = sb.toString();
            }
            response.setHeader("Content-Disposition", contentDispositionString);

            // Output the part
            final InputStream in = part.getInputStream();
            final ServletOutputStream out = response.getOutputStream();

            final byte[] b = new byte[1024];
            int len;
            try {
                while ((len = in.read(b)) != -1) {
                    out.write(b, 0, len);
                }
            } catch (SocketException e) {
                logger.error("This is most likely caused by the user cancling the attachment download.", e);
                throw e;
            }
        } finally {
            // Before the finally we were leaking JavaMail connections when the user cancled.
            Util.releaseFolder(folder); // clean up
        }
        return null;
    }

    private Part fetchBodyPart(final AttachmentForm form, final Message message, final HttpServletRequest request) throws MessagingException, IOException {
        final Part part;
        final String path = form.getPart();
        final String cid = form.getCid();

        if (path != null) {
            if ("".equals(path)) {
                part = message;
            } else {
                part = fetchBodyPartFromPath(path, message, request);
            }
        } else {
            part = fetchBodyPartFromCid(cid, message, request);
        }

        return part;
    }

    private BodyPart fetchBodyPartFromPath(final String path, final Message message, final HttpServletRequest request) throws IOException, MessagingException {
        final Object content = message.getContent();

        final BodyPart bodyPart;
        if (content instanceof Multipart) {
            final Multipart multipart = (Multipart)content;
            bodyPart = getContent(new StringTokenizer(path, "."), multipart);
        } else if (content instanceof Message) {
            // Skip past any nested messages
            bodyPart = fetchBodyPartFromPath(path, (Message)content, request);
        } else {
            bodyPart = null;
        }
        request.setAttribute("part", bodyPart);
        return bodyPart;
    }

    private BodyPart fetchBodyPartFromCid(String cid, final Message message, final HttpServletRequest request) throws IOException, MessagingException {
        cid = "<" + cid + ">";
        final Multipart multipart = (Multipart)message.getContent();
        BodyPart bodyPart = null;

        bodyPart = getContent(cid, multipart);

        request.setAttribute("part", bodyPart); // XXX Do we need this?
        return bodyPart;
    }

    /**
     * Reentrant form of {@link #fetchBodyPart(edu.ufl.osg.webmail.forms.AttachmentForm, javax.mail.Message, javax.servlet.http.HttpServletRequest)}.
     */
    private BodyPart fetchBodyPartFromCid(final String cid, final Message message) throws IOException, MessagingException {
        return getContent(cid, (Multipart)message.getContent());
    }

    private BodyPart getContent(final String cid, final Multipart part) throws MessagingException, IOException {
        BodyPart bodyPart = null;
        if (part instanceof MimeMultipart) {
            final MimeMultipart mPart = (MimeMultipart)part;
            bodyPart = mPart.getBodyPart(cid);
        }
        if (bodyPart == null) {
            for (int i = part.getCount() - 1; i >= 0; i--) {
                final BodyPart bp = part.getBodyPart(i);
                if (!bp.isMimeType("image/*")) {
                    final Object o = bp.getContent();
                    if (o instanceof Multipart) {
                        bodyPart = getContent(cid, (Multipart)o);
                    } else if (o instanceof Message) {
                        final Message nestedMessage = (Message)o;
                        bodyPart = fetchBodyPartFromCid(cid, nestedMessage);
                    }
                }
                if (bodyPart != null) {
                    break;
                }
            }
        }
        return bodyPart;
    }

    private BodyPart getContent(final StringTokenizer path, final Multipart part) throws MessagingException, IOException {
        final int partNumber = Integer.parseInt(path.nextToken());
        BodyPart bodyPart = part.getBodyPart(partNumber);

        if (path.hasMoreTokens()) {
            final Object o = bodyPart.getContent();
            if (o instanceof Multipart) {
                bodyPart = getContent(path, (Multipart)o);
            } else if (o instanceof Message) {
                bodyPart = getContent(path, (Multipart)((Message)o).getContent());
            }
        }

        return bodyPart;
    }
}
