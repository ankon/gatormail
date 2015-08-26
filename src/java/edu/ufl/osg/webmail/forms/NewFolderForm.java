/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002-2004 William A. McArthur, Jr.
 * Copyright (C) 2003-2005 The Open Systems Group / University of Florida
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

package edu.ufl.osg.webmail.forms;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Form for creating/renaming a folder.
 *
 * @author drakee
 * @author sandymac
 * @version $Revision: 1.5 $
 */
public final class NewFolderForm extends FolderForm {
    private static final Logger logger = Logger.getLogger(NewFolderForm.class.getName());

    /** Holds value of property newFolder. */
    private String newFolder;

    /**
     * Resets the <code>ActionForm</code> and then resets <code>folder</code>
     * to <code>null</code>.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(final ActionMapping mapping, final HttpServletRequest request) {
        super.reset(mapping, request);
        setNewFolder(null);
    }

    /**
     * Calls {@link FormsUtil#validateFolder(String,ActionErrors)}.
     *
     * @param  mapping             The mapping used to select this instance
     * @param  request             The servlet request we are processing
     * @return                     Errors, if any are found.
     * @see
     *                             FormsUtil#validateFolder(String,ActionErrors)
     */
    public ActionErrors validate(final ActionMapping mapping, final HttpServletRequest request) {
        final ActionErrors errors = super.validate(mapping, request);

        FormsUtil.validateFolder(getNewFolder(), errors);

        return errors;
    }

    /**
     * Getter for property newFolder.
     *
     * @return Value of property newFolder.
     */
    public String getNewFolder() {
        return this.newFolder;
    }

    /**
     * Setter for property newFolder.
     *
     * @param newFolder New value of property newFolder.
     */
    public void setNewFolder(final String newFolder) {
        this.newFolder = newFolder;
    }

}
