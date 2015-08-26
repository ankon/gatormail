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

package edu.ufl.osg.webmail.forms;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;


/**
 * Holds values used by the MoveCopy action (moves or copies one or more messages).
 *
 * @author drakee
 * @author sandymac
 * @version $Revision: 1.3 $
 */
public final class MoveCopyForm extends MultiMessageForm {

    /** Which folder to move a message to. */
    private String toFolder;

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
        setToFolder(null);
    }

    /**
     * Calls the super class's validate method and makes sure the toFolder field
     * is populated.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     *
     * @return Errors, if any are found.
     *
     * @see MultiMessageForm#validate(ActionMapping, HttpServletRequest)
     */
    public ActionErrors validate(final ActionMapping mapping, final HttpServletRequest request) {
        final ActionErrors errors = super.validate(mapping, request);

        if (getToFolder() == null || getToFolder().trim().equals("")) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.moveCopy.toFolder.required"));
        }

        return errors;
    }

    /**
     * Getter for property toFolder.
     *
     * @return Value of property toFolder.
     */
    public String getToFolder() {
        return this.toFolder;
    }

    /**
     * Setter for property toFolder.
     *
     * @param toFolder New value of property toFolder.
     */
    public void setToFolder(final String toFolder) {
        this.toFolder = toFolder;
    }
}
