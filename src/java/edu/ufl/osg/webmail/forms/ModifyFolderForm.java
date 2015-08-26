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

package edu.ufl.osg.webmail.forms;

import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;


/**
 * Holds values used by the ModifyFolderAction, submitted from Folder view.
 *
 * @author sandymac
 * @author drakee
 * @version $Revision: 1.2 $
 */
public final class ModifyFolderForm extends MultiMessageForm {

    /** Which folder to move a message to. */
    private String toFolder;

    /** Holds value of property deleteForever. */
    private boolean deleteForever;

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
        setDeleteForever(false);
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

    /**
     * Getter for property deleteForever.
     *
     * @return Value of property deleteForever.
     */
    public boolean getDeleteForever() {
        return deleteForever;
    }

    /**
     * Setter for property deleteForever.
     *
     * @param deleteForever New value of property deleteForever.
     */
    public void setDeleteForever(final boolean deleteForever) {
        this.deleteForever = deleteForever;
    }
}
