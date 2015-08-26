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

import edu.ufl.osg.webmail.Constants;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Holds values required for one or more Address Book entries.
 *
 * @author drakee
 * @version $Revision: 1.3 $
 */
public final class AddressesForm extends MessageForm {
    private static final Logger logger = Logger.getLogger(AddressesForm.class.getName());

    private String[] name;
    private String[] email;

    /**
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(final ActionMapping mapping, final HttpServletRequest request) {
        setName(null);
        setEmail(null);
    }

    /**
     * Verifies the name and email form a valid RFC 822 email adddress as
     * understood by {@link javax.mail.internet.InternetAddress#validate()}.
     *
     * @param  mapping             The mapping used to select this instance
     * @param  request             The servlet request we are processing
     * @return                     Errors, if any are found.
     */
    public ActionErrors validate(final ActionMapping mapping, final HttpServletRequest request) {
        // validates folder, message
        final ActionErrors errors = super.validate(mapping, request);

        final String[] name = getName();
        final String[] email = getEmail();
        logger.debug("there are " + name.length + " name entries");
        logger.debug("there are " + email.length + " email entries");

        final String[] values = request.getParameterValues(Constants.IS_SELECTED);
        if (values == null || (name == null || name.length == 0) || (email == null || email.length == 0)) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.saveAddresses.choice.required"));
            return errors;
        }

        for (int i = 0; i < values.length; i++) {
            final int value = Integer.parseInt(values[i]);
            logger.debug("validating " + email[value]);
            FormsUtil.validateAddress(email[value], name[value], errors);
        }
        return errors;
    }

    /**
     * Getter for property email.
     *
     * @return Value of property email.
     */
    public String[] getEmail() {
        return email != null ? (String[])email.clone() : null;
    }

    /**
     * Setter for property email.
     *
     * @param email New value of property email.
     */
    public void setEmail(final String[] email) {
        this.email = email != null ? (String[])email.clone() : null;
    }

    /**
     * Getter for property name.
     *
     * @return Value of property name.
     */
    public String[] getName() {
        return name != null ? (String[])name.clone() : null;
    }

    /**
     * Setter for property name.
     *
     * @param name New value of property name.
     */
    public void setName(final String[] name) {
        this.name = name != null ? (String[])name.clone() : null;
    }
}
