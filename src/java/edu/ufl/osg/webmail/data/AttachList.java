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

import org.apache.log4j.Logger;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This is the Container for holding a compose session's attachments. This
 * is what is retrieved from the AttachDAO, and it is what what you set
 * back into the DAO. All AttachObj objects are accessed through here.
 *
 * @author drakee
 * @version $Revision: 1.3 $
 */
public final class AttachList implements List, java.io.Serializable {
    private static final Logger logger = Logger.getLogger(AttachList.class.getName());
    private static final String DEFAULT_ATTACHMENT_NAME = "attachment";
    private static final String DEFAULT_FORWARD_NAME = "message";

    private List aoList;
    private transient AttachDAO attachDAO;

    // constructor
    public AttachList() throws AttachDAOException {
        aoList = new ArrayList();
    }

    // getter for attachDAO
    private AttachDAO getAttachDAO() throws AttachDAOException {
	// attachDAO may be null if session persistence was used,
	// since it is a transient member.
	if (attachDAO == null) {
	    attachDAO = DAOFactory.getInstance().getAttachDAO();
	}
	return attachDAO;
    }

    /**
     * Chooses the best file name for an attachment, since it is often null
     * by default. Makes sure that it is unique compared to the file names
     * in the passed in attachment Map.
     */
    public String generateFileName(final String fileName, final String contentType) {
        return generateFileName(fileName, contentType, false);
    }

    /**
     * Chooses the best file name for an attachment, since it is often null
     * by default. Makes sure that it is unique compared to the file names
     * in the passed in attachment Map.
     */
    public String generateFileName(String fileName, final String contentType, final boolean isForward) {
        logger.debug("> generateFileName() begin.");
        boolean usingDefaultName = false;
        if (fileName == null || fileName.length() == 0) {
            usingDefaultName = true;
            fileName = isForward ? DEFAULT_FORWARD_NAME : DEFAULT_ATTACHMENT_NAME;
            logger.debug("generateFileName() - using default name: " + fileName);
            logger.debug("...contentType: " + contentType);
        }
        // if making up a name for a plain text attachment,
        // we want to add a .txt suffix to prevent sending it as
        // an "application octet/stream"
        if (!isForward && usingDefaultName && contentType != null && contentType.toLowerCase().startsWith("text/plain")) {
            fileName += ".txt";
        }
        final String origName = fileName;
        for (int counter = 1; isDuplicateFileName(fileName); counter++) {
            fileName = counter + origName;
        }
        logger.debug("generated fileName: " + fileName);

        logger.debug("> generateFileName() end.");
        return fileName;
    }

    /**
     * Determines if there is an AttachObj in this List with the
     * given file name.
     * @return true if filename is contained in one of the list's AttachObjs.
     */
    public boolean containsFileName(final String fileName) {
        if (fileName == null)
            return false;
        final int size = aoList.size();
        for (int i = 0; i < size; i++) {
            final AttachObj attachObj = (AttachObj)aoList.get(i);
            if (fileName.equals(attachObj.getFileName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an AttachObj in this List with the
     * given file name.
     * @return the AttachObj with the passed in fileName
     */
    public AttachObj getByFileName(final String fileName) {
        if (fileName == null)
            return null;
        final int size = aoList.size();
        for (int i = 0; i < size; i++) {
            final AttachObj attachObj = (AttachObj)aoList.get(i);
            if (fileName.equals(attachObj.getFileName())) {
                return attachObj;
            }
        }
        return null;
    }

    /**
     * Returns an AttachObj in this List with the
     * given temp name.
     * @return the AttachObj with the passed in tempName
     */
    public AttachObj getByTempName(final String tempName) {
        if (tempName == null)
            return null;
        final int size = aoList.size();
        for (int i = 0; i < size; i++) {
            final AttachObj attachObj = (AttachObj)aoList.get(i);
            if (tempName.equals(attachObj.getTempName())) {
                return attachObj;
            }
        }
        return null;
    }

    /**
     * Adds an AttachObj to this List, as well as writing file data
     * to the backend storage.
     */
    public void addAttachment(final AttachObj attachObj, final byte[] fileData) throws AttachDAOException {
        getAttachDAO().setFile(attachObj, fileData);
        add(attachObj);
    }

    /**
     * Removes an AttachObj with the given fileName from this list, as well
     * as from the backend store.
     * @return true if remove was successful
     */
    public boolean removeByFileName(final String fileName) throws AttachDAOException {
        logger.debug("attempting to remove fileName: " + fileName + " from AttachList");
        if (fileName == null)
            return false;
        final Iterator it = aoList.iterator();
        while (it.hasNext()) {
            final AttachObj attachObj = (AttachObj)it.next();
            if (fileName.equals(attachObj.getFileName())) {
                logger.debug("... found it!");
                // remove from backend storage
                getAttachDAO().removeFile(attachObj);
                // remove from this list
                return aoList.remove(attachObj);
            }
        }
        logger.debug("... did not find it");
        return false;
    }

    /**
     * Removes an AttachObj with the given tempName from this list, as well
     * as from the backend store.
     * @return true if remove was successful
     */
    public boolean removeByTempName(final String tempName) throws AttachDAOException {
        logger.debug("attempting to remove tempName: " + tempName + " from AttachList");
        if (tempName == null)
            return false;
        final Iterator it = aoList.iterator();
        while (it.hasNext()) {
            final AttachObj attachObj = (AttachObj)it.next();
            if (tempName.equals(attachObj.getTempName())) {
                logger.debug("... found it!");
                // remove from backend storage
                getAttachDAO().removeFile(attachObj);
                // remove from this list
                return aoList.remove(attachObj);
            }
        }
        logger.debug("... did not find it");
        return false;
    }

    /**
     * Get a list of MimeBodyParts from this List
     */
    public List getMimeBodyPartList() throws AttachDAOException {
        return getAttachDAO().getMimeBodyPartList(this);
    }

    /**
     * Calculates and returns the total size of all attachments
     * contained in this list.
     * @return the combined size of all attachments
     */
    public int getCombinedSize() {
        int combSize = 0; // file sizes
        final int size = aoList.size();
        for (int i = 0; i < size; i++) {
            final AttachObj attachObj = (AttachObj)aoList.get(i);
            combSize += attachObj.getSize();
        }
        return combSize;
    }

    public String toString() {
        final StringBuffer buff = new StringBuffer();
        final int size = aoList.size();
        for (int i = 0; i < size; i++) {
            final AttachObj attachObj = (AttachObj)aoList.get(i);
            buff.append(attachObj.getFileName());
            if (i < size - 1)
                buff.append(", ");
        }
        return buff.toString();
    }

    ////////////////////////////////////////////////////////////
    // satisfy the List interface:
    ////////////////////////////////////////////////////////////
    public void add(final int index, final Object element) {
        try {
            final AttachObj attachObj = processAttachObj(element);
            if (attachObj == null) return;
            aoList.add(index, attachObj);
        } catch (Exception e) {
            logger.error(e.toString());
            return;
        }
    }

    public boolean add(final Object o) {
        try {
            final AttachObj attachObj = processAttachObj(o);
            if (attachObj == null) return false;
            return aoList.add(attachObj);
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public boolean addAll(final Collection c) {
        final Iterator it = c.iterator();
        try {
            while (it.hasNext()) {
                final Object o = it.next();
                final AttachObj attachObj = processAttachObj(o);
                if (attachObj == null) return false;
                aoList.add(attachObj);
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
        return true;
    }

    public boolean addAll(final int index, final Collection c) {
        final Iterator it = c.iterator();
        try {
            while (it.hasNext()) {
                final Object o = it.next();
                final AttachObj attachObj = processAttachObj(o);
                if (attachObj == null) return false;
                aoList.add(index, attachObj);
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
        return true;
    }

    public void clear() {
        try {
            getAttachDAO().clearAttachList(this);
        } catch (AttachDAOException e) {
            throw new RuntimeException(e);
        }
        aoList.clear();
    }

    public boolean contains(final Object o) {
        return aoList.contains(o);
    }

    public boolean containsAll(final Collection c) {
        return aoList.containsAll(c);
    }

    public boolean equals(final Object o) {
        return aoList.equals(o);
    }

    public Object get(final int index) {
        return aoList.get(index);
    }

    public int hashCode() {
        return aoList.hashCode();
    }

    public int indexOf(final Object o) {
        return aoList.indexOf(o);
    }

    public boolean isEmpty() {
        return aoList.isEmpty();
    }

    public Iterator iterator() {
        return aoList.iterator();
    }

    public int lastIndexOf(final Object o) {
        return aoList.lastIndexOf(o);
    }

    public ListIterator listIterator() {
        return aoList.listIterator();
    }

    public ListIterator listIterator(final int index) {
        return aoList.listIterator(index);
    }

    public Object remove(final int index) {
        return aoList.remove(index);
    }

    public boolean remove(final Object o) {
        return aoList.remove(o);
    }

    public boolean removeAll(final Collection c) {
        return aoList.removeAll(c);
    }

    public boolean retainAll(final Collection c) {
        return aoList.retainAll(c);
    }

    public Object set(final int index, final Object element) {
        try {
            final AttachObj attachObj = processAttachObj(element);
            if (attachObj == null) return null;
            return aoList.set(index, element);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public int size() {
        return aoList.size();
    }

    public List subList(final int fromIndex, final int toIndex) {
        return aoList.subList(fromIndex, toIndex);
    }

    public Object[] toArray() {
        return aoList.toArray();
    }

    public Object[] toArray(final Object[] a) {
        return aoList.toArray();
    }

    ////////////////////////////////////////////////////////////
    // private utility methods
    ////////////////////////////////////////////////////////////
    /**
     * Verifies that the passed-in object is an <code>AttachObj</code>, and if necessary,
     * changes the <code>attachObj</code>'s filename so it doesn't duplicate any in the
     * <code>AttachList</code>.
     * @return the "processed" AttachObj, or null if the object passed in was
     * invalid.
     */
    private AttachObj processAttachObj(final Object o) {
        if (!(o instanceof AttachObj)) return null;
        final AttachObj attachObj = (AttachObj)o;
        // check to see if any of the other attachments in the list
        // have the same filename
        final String origName = attachObj.getFileName();
        if (origName == null || isDuplicateFileName(origName)) {
            // found duplicate in list, change the new attachment's name
            final String fileName = generateFileName(origName, attachObj.getContentType(), attachObj.getIsForward());
            attachObj.setFileName(fileName);
        }
        return attachObj;
    }

    /**
     * is the fileName duplicated in our list of AttachObjs?
     */
    private boolean isDuplicateFileName(final String fileName) {
        final int size = aoList.size();
        for (int i = 0; i < size; i++) {
            final AttachObj attachObj = (AttachObj)aoList.get(i);
            if (fileName.equals(attachObj.getFileName()))
                return true;
        }
        return false;
    }
}
