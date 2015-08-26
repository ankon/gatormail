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
 * Attachment controller.
 *
 * @author sandymac
 * @version $Revision: 1.3 $
 */
public final class AttachmentForm extends MessageForm {
    /**
     * Holds value of property part.
     */
    private String part;

    /**
     * Holds the value of the property cid.
     */
    private String cid;

    /**
     * Resets the MessageForm and then sets part to <code>null</code>.
     *
     * @param mapping             The mapping used to select this instance
     * @param request             The servlet request we are processing
     */
    public void reset(final ActionMapping mapping, final HttpServletRequest request) {
        super.reset(mapping, request);
        setPart(null);
    }

    /**
     * Calls the super class's validate method and {@link
     * #validateAttachment(ActionErrors) validateAttachment}.
     *
     * @param  mapping             The mapping used to select this instance
     * @param  request             The servlet request we are processing
     * @return                     Errors, if any are found.
     * @see    #validateAttachment(ActionErrors)
     */
    public ActionErrors validate(final ActionMapping mapping, final HttpServletRequest request) {
        final ActionErrors errors = super.validate(mapping, request);

        // Part parameter?
        validateAttachment(errors);

        return errors;
    }

    /**
     * Checks that <code>part</code> is not <code>null</code>.
     *
     * @param errors              The ActionErrors to append any error messages
     *                            to.
     */
    protected void validateAttachment(final ActionErrors errors) {
        if (getPart() == null && getCid() == null) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("message.invalid.part"));
        } else if (getPart() != null && getCid() != null) {
            // XXX: Too many parts
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("message.invalid.part"));
        }
    }

    public String getCid() {
        return cid;
    }

    public void setCid(final String cid) {
        this.cid = cid;
    }

    /**
     * Getter for property part.
     *
     * @return                     Value of property part.
     */
    public String getPart() {
        return this.part;
    }

    /**
     * Setter for property part.
     *
     * @param part                New value of property part.
     */
    public void setPart(final String part) {
        this.part = part;
    }
}
