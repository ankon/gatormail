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

package edu.ufl.osg.webmail.data;

import java.util.List;

/**
 * Interface for interacting with the attachment store.
 *
 * @author drakee
 * @version $Revision: 1.2 $
 */
public interface AttachDAO {
    /**
     * Clears out all stored files for this user
     */
    public void clearAttachList(List attachList) throws AttachDAOException;

    /**
     * Adds a file to the store. Returns an AttachObj that
     * represents the stored file.
     */
    public void removeFile(AttachObj attachObj) throws AttachDAOException;

    /**
     * Adds a file to the store.
     */
    public void setFile(AttachObj attachObj, byte[] data) throws AttachDAOException;

    /**
     * Retrieves all files from the store, puts them into MimeBodyPart format,
     * and ships them in a List. This method is expensive and should only
     * be called when the email is about to be sent.
     */
    public List getMimeBodyPartList(AttachList attachList) throws AttachDAOException;
}
