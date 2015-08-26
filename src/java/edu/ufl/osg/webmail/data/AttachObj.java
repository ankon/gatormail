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

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Basically a wrapper for MimeBodyPart.
 * Represents one piece of attachment.
 *
 * @author drakee
 * @version $Revision: 1.2 $
 */
public final class AttachObj implements java.io.Serializable {
    private static final Logger logger = Logger.getLogger(AttachObj.class.getName());

    private String fileName;
    private String tempName;
    private String contentType;
    private String displayContentType;
    private boolean init_displayContType = false;
    private String disposition;
    private String description;
    private int size;
    private boolean isForward = false;

    // forwarded message-specific fields
    private String folderFullName;
    private Long uid;
    private Integer messageNumber;

    /**
     * Creates a new instance of AttachObj
     */
    public AttachObj() {
    }

    /**
     * Creates a new instance of AttachObj
     */
    public AttachObj(final MimeBodyPart bodyPart) throws IOException, MessagingException {

        // file name
        setFileName(bodyPart.getFileName());

        // content type
        final String contentType = bodyPart.getContentType();
        setContentType(contentType);

        // disposition
        setDisposition(bodyPart.getDisposition());

        // description
        setDescription(bodyPart.getDescription());

        // size
        if (bodyPart.getSize() < 0) {
            // for some reason, JavaMail MimeBodyParts like to report
            // themselves as 0 length. This gets the size straight from
            // the input stream.
            setSize(bodyPart.getInputStream().available());
        } else {
            setSize(bodyPart.getSize());
        }
    }

    /**
     * Creates a new instance of AttachObj. This constructor is used
     * for messages that are to be forwarded.
     */
    public AttachObj(final Message message, final Long uid, final Integer messageNumber) throws IOException, MessagingException {
        setIsForward(true);

        // file name
        setFileName(message.getFileName());

        // content type
        setContentType(message.getContentType());

        // disposition
        setDisposition(message.getDisposition());

        // description
        setDescription(message.getDescription());

        // size
        setSize(message.getSize());

        // folder
        setFolderFullName(message.getFolder().getFullName());

        // IDs
        setUid(uid);
        setMessageNumber(messageNumber);

        // tempName
        setTempName("forward");
    }

    ////////////////////////////////////////////////////////////
    // property Getters and Setters
    ////////////////////////////////////////////////////////////
    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getTempName() {
        return tempName;
    }

    public void setTempName(final String tempName) {
        logger.debug("setting tempName for " + getFileName() + ": " + tempName);
        this.tempName = tempName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getDisplayContentType() {
        if (!init_displayContType) {
            displayContentType = getContentType();
            final StringTokenizer st = new StringTokenizer(contentType, ";");
            if (st.hasMoreTokens()) {
                displayContentType = st.nextToken().toLowerCase();
            }
            init_displayContType = true;
        }
        return displayContentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(final String disposition) {
        this.disposition = disposition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    public String toString() {
        return getFileName();
    }

    ////////////////////////////////////////////////////////////
    // fields for forwarded message
    ////////////////////////////////////////////////////////////
    /**
     * Does this represent a forwarded message (as opposed to
     * a MimeBodyPart)?
     */
    public boolean getIsForward() {
        return isForward;
    }

    /**
     * Setter for property isForward.
     *
     * @param isForward New value of property isForward.
     */
    public void setIsForward(final boolean isForward) {
        this.isForward = isForward;
    }

    /**
     * Getter for property uid.
     *
     * @return Value of property uid.
     */
    public Long getUid() {
        return uid;
    }

    /**
     * Setter for property uid.
     *
     * @param uid New value of property uid.
     */
    public void setUid(final Long uid) {
        this.uid = uid;
    }

    /**
     * Getter for property messageNumber.
     */
    public Integer getMessageNumber() {
        return messageNumber;
    }

    /**
     * Setter for property messageNumber.
     *
     * @param messageNumber New value of property messageNumber.
     */
    public void setMessageNumber(final Integer messageNumber) {
        this.messageNumber = messageNumber;
    }

    /**
     * Getter for property folderFullName.
     */
    public String getFolderFullName() {
        return folderFullName;
    }

    /**
     * Setter for property folderFullName.
     *
     * @param folderFullName New value of property folderFullName.
     */
    public void setFolderFullName(final String folderFullName) {
        this.folderFullName = folderFullName;
    }

    /**
     * Makes sure to clean up back-end object
     */
    protected void finalize() throws Throwable {
        logger.debug("-- finalize() begin");
        super.finalize();
        try {
            if (!getIsForward()) {
                final AttachDAO attachDAO = DAOFactory.getInstance().getAttachDAO();
                attachDAO.removeFile(this);
            }
            logger.debug("-- finalize() end");
        } catch (Exception e) {
            logger.error("problem with AttachObj.finalize(): " + e.toString());
        }
    }

    ////////////////////////////////////////////////////////////
    // private utility methods
    ////////////////////////////////////////////////////////////
    /**
     * Returns a new copy of a MimeBodyPart, with a filename of your choice.
     */
    private static MimeBodyPart createMimeBodyPart(final MimeBodyPart bodyPart, final String fileName) throws IOException, MessagingException {
        final MimeBodyPart newPart = new MimeBodyPart();
        newPart.setFileName(fileName);
        newPart.setDataHandler(bodyPart.getDataHandler());
        newPart.setContent(bodyPart.getContent(), bodyPart.getContentType());
        newPart.setDisposition("attachment; filename=\"" + newPart.getFileName() + "\"");
        logger.debug("createNewMimeBodyPart():");
        logger.debug("...fileName: " + newPart.getFileName());
        logger.debug("...contentType: " + newPart.getContentType());
        return newPart;
    }
}
