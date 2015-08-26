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

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.internet.MimeBodyPart;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Implementation for interacting with the attachment store.
 *
 * @author drakee
 * @version $Revision: 1.4 $
 */
public class AttachDAO_FILE implements AttachDAO {
    private static final Logger logger = Logger.getLogger(AttachDAO_FILE.class.getName());
    private static final String ATTACH_PREFIX = "att";

    private String basePath;
    private File baseDir;

    protected AttachDAO_FILE() throws AttachDAOException {
        Properties props = null;
        try {
            final ConfigDAO configDAO = DAOFactory.getInstance().getConfigDAO();
            props = configDAO.getProperties();
        } catch (ConfigDAOException cde) {
            throw new AttachDAOException(cde.toString());
        }
        basePath = props.getProperty("attachmentPath");
        baseDir = new File(basePath);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        logger.debug("in constructor, basePath: " + basePath);
    }

    public void clearAttachList(final List attachList) throws AttachDAOException {
        try {
            for (int i = 0; i < attachList.size(); i++) {
                final AttachObj attachObj = (AttachObj)attachList.get(i);
                removeFile(attachObj);
            }
        } catch (Exception e) {
            throw new AttachDAOException(e.toString());
        }
    }

    public void removeFile(final AttachObj attachObj) throws AttachDAOException {
        try {
            final File file = new File(baseDir, attachObj.getTempName());
            if (file.delete())
                logger.debug("deleted tempFile: " + file);
            else
                logger.debug("did not find tempFile: " + file);
        } catch (Exception e) {
            throw new AttachDAOException(e.toString());
        }
    }

    public void setFile(final AttachObj attachObj, final byte[] data) throws AttachDAOException {
        try {
            // write attachment list to disk
            final File tempFile = File.createTempFile(ATTACH_PREFIX, filterSuffix(attachObj.getFileName()), baseDir);
            tempFile.deleteOnExit();
            attachObj.setTempName(tempFile.getName());

            // write out the data to file
            final FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(data);
            fos.close();
        } catch (Exception e) {
            throw new AttachDAOException(e.toString());
        }
    }

    public List getMimeBodyPartList(final AttachList attachList) throws AttachDAOException {
        final List lst = new ArrayList();
        logger.debug("> getMimeBodyPartList() begin");
        try {
            for (int i = 0; i < attachList.size(); i++) {
                final AttachObj attachObj = (AttachObj)attachList.get(i);
                // don't retrieve forwarded messages
                if (attachObj.getIsForward())
                    continue;

                final String fileName = attachObj.getTempName();
                logger.debug(".. going to load: " + basePath + File.separator + fileName);
                final FileDataSource fds = new FileDataSource(basePath + File.separator + fileName);
                final DataHandler dh = new DataHandler(fds);
                logger.debug(".. DataHandler contentType: " + dh.getContentType());
                final MimeBodyPart bodyPart = new MimeBodyPart();
                bodyPart.setDataHandler(dh);
                bodyPart.setFileName(attachObj.getFileName());
                if (attachObj.getDescription() != null) {
                    bodyPart.setDescription(attachObj.getDescription());
                }

                lst.add(bodyPart);
                logger.debug(".. created bodyPart: " + bodyPart.getFileName());
                logger.debug("... contentType: " + bodyPart.getContentType());
                logger.debug("... disposition: " + bodyPart.getDisposition());
            }
        } catch (Exception e) {
            throw new AttachDAOException(e.toString());
        }
        logger.debug("> getMimeBodyPartList() end");
        return lst;
    }

    ////////////////////////////////////////////////////////////
    // private utility methods
    ////////////////////////////////////////////////////////////
    private static final char[] legalSuffixChars = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    private static String filterSuffix(final String fileName) {
        // check if there is a suffix
        final int dot = fileName.lastIndexOf(".");
        if (dot < 0)
            return null; // default - will create .tmp suffix

        final String suffix = fileName.substring(dot + 1).toLowerCase();

        // check if the suffix has a length
        final int length = suffix.length();
        if (length < 1)
            return null; // default - will create .tmp suffix

        // make sure the suffix has "good" characters
        for (int i = 0; i < length; i++) {
            if (Arrays.binarySearch(legalSuffixChars, suffix.charAt(i)) < 0) {
                logger.debug("filterSuffix. binarySearch returned: " + Arrays.binarySearch(legalSuffixChars, suffix.charAt(i)));
                return null;
            }
        }
        return "." + suffix; // all's well
    }
}
