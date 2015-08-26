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

import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.mail.Folder;
import javax.mail.Message;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Printer-friendly message view controller.
 *
 * @author drakee
 * @version $Revision: 1.2 $
 */
public class PrinterFriendlyAction extends MessageAction {
    private static final Logger logger = Logger.getLogger(PrinterFriendlyAction.class.getName());

    /**
     * Sets up the request enviroment for the message view. The current Folder
     * and Message are put in the request attributes.
     *
     * @param     mapping             The ActionMapping used to select this
     *                                instance
     * @param     form                The optional ActionForm bean for this
     *                                request (if any)
     * @param     request             The HTTP request we are processing
     * @param     response            The HTTP response we are creating
     * @return                        An ActionForward instance to either the
     *                                message view or a failure view.
     * @exception Exception if the application business
     *                                logic throws an exception
     */
    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        ActionsUtil.checkSession(request);

        // Open the folder
        final Folder folder = ActionsUtil.fetchFolder(form, request);
        request.setAttribute("folder", folder);

        // Find the message
        final Message message = ActionsUtil.fetchMessage(form, folder);
        request.setAttribute("message", message);

        Util.releaseFolder(folder); // clean up

        // the message view looks for this attribute, to determine whether to
        // show the "Add to address book" link.
        request.setAttribute("printerFriendly", "true");

        return mapping.findForward("success");
    }
}
