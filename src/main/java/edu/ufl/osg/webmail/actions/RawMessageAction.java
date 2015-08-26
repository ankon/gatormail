/*
* This file is part of GatorMail, a servlet based webmail.
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

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionError;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Flags;
import javax.mail.Header;

import edu.ufl.osg.webmail.util.Util;

import java.util.Enumeration;
import java.io.Writer;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

/**
 * Outputs the raw contents of a message as text/plain.
 *
 * @author sandymac
 * @since Sep 22, 2003 5:46:32 PM
 * @version $Revision: 1.3 $
 */
public class RawMessageAction extends Action {
    private static final Logger logger = Logger.getLogger(RawMessageAction.class.getName());
    private static final byte[] CRLF_BYTES = "\r\n".getBytes(); // CR LF
    private static final byte[] COLON_BYTES = ": ".getBytes();

    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        logger.debug("=== RawMessageAction.execute() begin ===");
        ActionsUtil.checkSession(request);
        final ActionErrors errors = new ActionErrors();

        // Open the folder
        final Folder folder = ActionsUtil.fetchFolder(form, request);
        request.setAttribute("folder", folder);

        // Find the message
        final Message message = ActionsUtil.fetchMessage(form, folder);
        if (message == null) {
            Util.releaseFolder(folder); // clean up
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.message.notexist"));
            saveErrors(request, errors);
            return mapping.findForward("folder");
        }
        request.setAttribute("message", message);

        // mark message as read
        message.setFlag(Flags.Flag.SEEN, true);

        response.setContentType("text/plain");

        final OutputStream out = response.getOutputStream();

        final Enumeration hEnum = message.getAllHeaders();
        while (hEnum.hasMoreElements()) {
            final Header header = (Header)hEnum.nextElement();
            out.write(header.getName().getBytes());
            out.write(COLON_BYTES);
            out.write(header.getValue().getBytes());
            out.write(CRLF_BYTES);
        }
        out.write(CRLF_BYTES);

        final InputStream in = message.getInputStream();
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

        Util.releaseFolder(folder); // clean up

        logger.debug("=== RawMessageAction.execute() end ===");
        return null;
    }
}
