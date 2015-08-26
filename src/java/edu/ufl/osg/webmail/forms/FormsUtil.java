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

import edu.ufl.osg.webmail.Constants;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;

/**
 * Package private util class.
 *
 * @author drakee
 * @author sandymac
 * @version $Revision: 1.3 $
 */
final class FormsUtil {
    private static final Logger logger = Logger.getLogger(FormsUtil.class.getName());

    /**
     * Validates a name and email address.
     */
    protected static void validateAddress(final String email, final String name, final ActionErrors errors) {
        boolean errorFound = false;
        if (name == null || name.trim().equals("")) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.address.name.required"));
            errorFound = true;
        } else if (name.length() > Constants.MAXSIZE_NAME) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.address.name.toolong", String.valueOf(Constants.MAXSIZE_NAME)));
            errorFound = true;
        }

        if (email == null || email.trim().equals("")) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.address.email.required"));
            errorFound = true;
        } else if (email.length() > Constants.MAXSIZE_EMAIL) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.address.email.toolong", String.valueOf(Constants.MAXSIZE_EMAIL)));
            errorFound = true;
        }

        if (!errorFound) { // now validate that the InternetAddress is valid
            try {
                final InternetAddress internetAddress = new InternetAddress(email, name);
                internetAddress.validate();
            } catch (AddressException ae) {
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.address.email.invalid"));
            } catch (UnsupportedEncodingException uee) {
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.address.email.invalid"));
            }
        }
    }

    /**
     * Validates that either <code>messageNumber</code> or an <code>uid</code> is submitted.
     */
    protected static void validateMessage(final Integer messageNumber, final Long uid, final ActionErrors errors) {
        // Both MessageNumber and UID?? shouldn't happen.
        if ((messageNumber != null) && (uid != null)) {
            logger.error("messageNumber && uid are both not null");
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("message.invalid.messageids"));
        } else if ((messageNumber == null) && (uid == null)) {
            logger.error("messageNumber && uid are both blank");
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.messageList.message.required"));
        }
    }

    /**
     * Validates that a <code>messageNumber</code> or an <code>uid</code> array is submitted.
     */
    protected static void validateMessages(final Integer[] messageNumber, final Long[] uid, final ActionErrors errors) {
        // XXX: We may need a different ActionError message
        if ((messageNumber != null) && (uid != null)) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("message.invalid.messageids"));
        } else if ((messageNumber == null || messageNumber.length == 0) && (uid == null || uid.length == 0)) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.messageList.message.required"));
        }
    }

    /**
     * Checks that the <code>folder</code> property is not <code>null</code>.
     *
     * @param errors The ActionErrors to append possible error messages to.
     */
    protected static void validateFolder(final String folder, final ActionErrors errors) {
        if (folder == null) {
            logger.debug("error: folder is null");
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("message.invalid.folder"));
        }
    }
}
