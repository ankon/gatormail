/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002-2004 William A. McArthur, Jr.
 * Copyright (C) 2003-2004 The Open Systems Group / University of Florida
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

package edu.ufl.osg.webmail.forms;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;


/**
 * Simple form with folder and action field, useful for LookupDispatchActions.
 *
 * @author drakee
 * @version $Revision: 1.4 $
 */
public final class FolderActionForm extends FolderForm {
    private static final Logger logger = Logger.getLogger(FolderActionForm.class.getName());

    /** the action to be performed */
    private String action;

    /**
     * Resets the {@link FolderForm FolderForm} and then resets
     * <code>MessageNumber</code>, <code>UID</code>, and <code>Action</code>
     * to <code>null</code>.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(final ActionMapping mapping, final HttpServletRequest request) {
        super.reset(mapping, request);
        setAction(null);
    }

    /**
     * Calls {@link FolderForm#validate(ActionMapping, HttpServletRequest)}.
     * Checks that only one of MessageNumber or UID is set.
     * Checks that at least one of MessageNumber or UID is set.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     *
     * @return Errors, if any are found.
     *
     * @see FolderForm#validate(ActionMapping, HttpServletRequest)
     */
    public ActionErrors validate(final ActionMapping mapping, final HttpServletRequest request) {
        final ActionErrors errors = super.validate(mapping, request);

        // action must be specified
        if (getAction() == null || getAction().trim().equals("")) {
            logger.error("error: action is null");
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.action.required"));
        }

        return errors;
    }

    /**
     * Getter for property action.
     *
     * @return Value of property action.
     */
    public String getAction() {
        return this.action;
    }

    /**
     * Setter for property action.
     *
     * @param action New value of property action.
     */
    public void setAction(final String action) {
        this.action = action;
    }
}
