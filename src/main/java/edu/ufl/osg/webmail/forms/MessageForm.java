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

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;


/**
 * Message view form.
 *
 * @author sandymac
 * @version $Revision: 1.3 $
 */
public class MessageForm extends FolderForm {
    private static final Logger logger = Logger.getLogger(MessageForm.class.getName());

    /** Holds value of property messageNumber. */
    private Integer messageNumber;

    /** Holds value of property uid. */
    private Long uid;

    /**
     * Resets the {@link FolderForm FolderForm} and then resets
     * <code>MessageNumber</code> and <code>UID</code> to <code>null</code>.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(final ActionMapping mapping, final HttpServletRequest request) {
        super.reset(mapping, request);
        setMessageNumber(null);
        setUid(null);
    }

    /**
     * Calls the super class's validate method and {@link FormsUtil#validateMessage(Integer, Long, ActionErrors)}.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     *
     * @return Errors, if any are found.
     *
     * @see FormsUtil#validateMessage
     */
    public ActionErrors validate(final ActionMapping mapping, final HttpServletRequest request) {
        final ActionErrors errors = super.validate(mapping, request);

        FormsUtil.validateMessage(getMessageNumber(), getUid(), errors);

        return errors;
    }

    /**
     * Getter for property messageNumber.
     *
     * @return Value of property messageNumber.
     */
    public Integer getMessageNumber() {
        return this.messageNumber;
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
     * Getter for property uid.
     *
     * @return Value of property uid.
     */
    public Long getUid() {
        return this.uid;
    }

    /**
     * Setter for property uid.
     *
     * @param uid New value of property Uid.
     */
    public void setUid(final Long uid) {
        this.uid = uid;
    }
}
