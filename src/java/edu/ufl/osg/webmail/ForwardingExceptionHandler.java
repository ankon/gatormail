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

package edu.ufl.osg.webmail;

import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ExceptionHandler;
import org.apache.struts.config.ExceptionConfig;
import org.apache.struts.util.ModuleException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author sandymac
 * @version $Revision: 1.3 $
 */
public class ForwardingExceptionHandler extends ExceptionHandler {
    private static final Logger logger = Logger.getLogger(ForwardingExceptionHandler.class.getName());

    public ActionForward execute(final Exception ex, final ExceptionConfig ae, final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        //        System.out.println("HERE: " + ae.getPath());
        ActionForward forward = null;
        ActionError error = null;
        //String property = null;

        // Build the forward from the exception mapping if it exists
        // or from the form input
        if (ae.getPath() != null) {
            //forward = new ActionForward(ae.getPath());
            forward = mapping.findForward(ae.getPath());
        } else {
            forward = mapping.getInputForward();
        }

        // Generate the forward
        //forward = new ActionForward(path);
        // Figure out the error
        if (ex instanceof ModuleException) {
            error = ((ModuleException)ex).getError();
            //property = ((ModuleException)ex).getProperty();
        } else {
            error = new ActionError(ae.getKey(), ex.getMessage());
            //property = error.getKey();
        }

        // since stack traces are rather expensive, don't do it if the
        // exception is a run of the mill "session expired" or
        // "invalid session" error
        if (!(ex instanceof InvalidSessionException)) {
            // show who's having this error
            final User user = Util.getUser(request.getSession());
            //  	    StringBuffer sb = new StringBuffer();
            //  	    if (user != null) {
            //  		sb.append("ERROR! (user: " + user.getUsername() + ", name: "
            //  			  + user.getDisplayName() + ", id: " + user.getPermId() + ")\n");
            //  	    }
            //  	    // capture stack trace
            //  	    StringWriter sw = new StringWriter();
            //  	    ex.printStackTrace(new PrintWriter(sw));
            //  	    StringBuffer errSb = sw.getBuffer();
            //  	    // truncate if stack trace is overly long
            //  	    sb.append((errSb.length() > 2048) ? errSb.substring(0, 2048) : errSb.toString());
            //  	    // output to error log
            //  	    logger.error(sb.toString());

            // output to error log
            logger.error(Util.buildErrorMessage(ex, user));
        }

        // Store the exception
        request.setAttribute(Globals.EXCEPTION_KEY, ex);
        storeException(request, ActionErrors.GLOBAL_ERROR, error, forward, ae.getScope());

        return forward;
    }
}
