/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002, 2003 William A. McArthur, Jr.
 * Copyright (C) 2003 The Open Systems Group / University of Florida
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

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Verifies that there is a valid, logged in session to verify that cookies are
 * working. The should should only be sent to this action when we think they
 * should be logged in and the only reason they won't be would be the lack of
 * cookies.
 *
 * @author sandymac
 * @version $Revision: 1.5 $
 * @deprecated Shouldn't be needed since we moved away from WebShere.
 */
public class CheckCookiesAction extends Action {
    private static final Logger logger = Logger.getLogger(CheckCookiesAction.class.getName());

    /**
     * Verifyies that the user has a valid http session.
     *
     * @param  mapping             The ActionMapping used to select this
     *                             instance
     * @param  form                The optional ActionForm bean for this request
     *                             (if any)
     * @param  request             The HTTP request we are processing
     * @param  response            The HTTP response we are creating
     * @return                     An ActionForward back to the login page.
     * @throws Exception           if the application business logic throws an
     *                             exception
     */
    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        ActionForward forward;
        try {
            ActionsUtil.checkSession(request);
            forward = mapping.findForward("success");
        } catch (Exception e) {
            // This was done this way becuase some containers don't include the JSESSIONID
            // cookie in the request.getCookies()
            final List headers = new ArrayList();
            final Enumeration hEnum = request.getHeaderNames();
            while (hEnum.hasMoreElements()) {
                final String header = (String)hEnum.nextElement();
                final Enumeration hEnum2 = request.getHeaders(header);
                while (hEnum2.hasMoreElements()) {
                    headers.add(header + ": " + hEnum2.nextElement());
                }
            }
            logger.debug("A user seems not to have cookies enabled: " + headers);
            forward = mapping.findForward("fail");
        }

        return forward;
    }
}
