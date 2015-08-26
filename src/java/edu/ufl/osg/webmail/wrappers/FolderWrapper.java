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

package edu.ufl.osg.webmail.wrappers;

import edu.ufl.osg.webmail.util.Util;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;


/**
 * @author sandymac
 * @version $Revision: 1.2 $
 */
public class FolderWrapper {
    /** Holds value of property JavaMailFolder. */
    private Folder javaMailFolder;

    /**
     * Creates a new instance of FolderWrapper
     */
    public FolderWrapper(final Folder folder) {
        setJavaMailFolder(folder);
    }

    /**
     * Getter for property javaMailFolder.
     *
     * @return Value of property folder.
     */
    public Folder getJavaMailFolder() {
        return this.javaMailFolder;
    }

    /**
     * Setter for property javaMailFolder.
     *
     * @param javaMailFolder      New value of property folder.
     */
    protected void setJavaMailFolder(final Folder javaMailFolder) {
        this.javaMailFolder = javaMailFolder;
    }

    /**
     * Getter for property fullName.
     *
     * @return Value of property fullName.
     */
    public String getFullName() {
        return javaMailFolder.getFullName();
    }

    /**
     * Getter for property messageCount.
     *
     * @return Value of property messageCount.
     */
    public int getMessageCount() throws MessagingException {
        return javaMailFolder.getMessageCount();
    }

    /**
     * Getter for property name.
     *
     * @return Value of property name.
     */
    public String getName() {
        return javaMailFolder.getName();
    }

    public FolderWrapper[] list() throws MessagingException {
        final Folder[] folders = javaMailFolder.list();
        final FolderWrapper[] folderWrappers = new FolderWrapper[folders.length];

        for (int i = 0; i < folders.length; i++) {
            // Reuse 'heavy weight' folders when we can.
            folderWrappers[i] = new FolderWrapper(Util.getFolder(folders[i]));
        }

        return folderWrappers;
    }
}