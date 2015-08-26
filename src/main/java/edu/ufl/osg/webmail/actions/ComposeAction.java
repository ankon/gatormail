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
import edu.ufl.osg.webmail.forms.ComposeForm;
import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Set up the view for the compose page.
 *
 * @author sandymac
 * @author drakee
 * @version $Revision: 1.2 $
 */
public class ComposeAction extends Action {
    private static final Logger logger = Logger.getLogger(ComposeAction.class.getName());

    /**
     * Composes an email. This is the first page view of a "compose message"
     * session, and we don't return to this action until after the user has left
     * or sent the message.
     *
     * @param     mapping             The ActionMapping used to select this
     *                                instance
     * @param     form                The optional ActionForm bean for this
     *                                request (if any)
     * @param     request             The HTTP request we are processing
     * @param     response            The HTTP response we are creating
     * @return                        The "success" <code>ActionForward</code>.
     * @exception Exception if the application business
     *                                logic throws an exception
     */
    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        logger.debug("=== ComposeAction.execute() begin ===");
        ActionsUtil.checkSession(request);

        final HttpSession session = request.getSession();
        final ComposeForm compForm = (ComposeForm)form;
        final User user = Util.getUser(session);

        // add signature to blank message body
        final PreferencesProvider pp = (PreferencesProvider)getServlet().getServletContext().getAttribute(Constants.PREFERENCES_PROVIDER);
        compForm.setBody("\r\n\r\n\r\n" + pp.getPreferences(user, session).getProperty("compose.signature"));

        // generate unique compose key for this view
        compForm.setComposeKey(Util.generateComposeKey(session));

        // Default CopyToSent to true.
        compForm.setCopyToSent(true);

        logger.debug("=== ComposeAction.execute() end ===");
        return mapping.findForward("success");
    }
}