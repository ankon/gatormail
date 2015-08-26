/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002, 2003 William A. McArthur, Jr.
 * Copyright (C) 2003,2006 The Open Systems Group / University of Florida
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
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;


/**
 * Login view form.
 *
 * @author sandymac
 * @version $Revision: 1.2 $
 */
public class LoginForm extends ActionForm {
    /** Holds value of property username. */
    private String username;

    /** Holds value of property password. */
    private String password;

    /** Holds value of property action. */
    private String action;

    /**
     * Reset all bean properties to their default state. This method is called
     * before the properties are repopulated by the controller servlet.
     *
     * @param mapping The mapping used to select this instance.
     * @param request The servlet request we are processing
     */
    public void reset(final ActionMapping mapping, final HttpServletRequest request) {
        setAction(null);

        //setUsername(null);
        setPassword(null);
    }

    /**
     * Make sure <code>username</code> and <code>password</code> are not
     * <code>null</code> or zero length.
     *
     * @param mapping The mapping used to select this instance.
     * @param request The servlet request we are processing.
     *
     * @return The ActionErrors or null or an ActionErrors object with no
     *         recorded error messages.
     */
    public ActionErrors validate(final ActionMapping mapping, final HttpServletRequest request) {
        final ActionErrors errors = new ActionErrors();
        final String action = getAction();
        final String username = getUsername();
        final String password = getPassword();

        if ((action == null) && (username == null) && (password == null)) {
            return errors;
        }

        if ((username == null) || (username.length() < 1)) {
            errors.add("username", new ActionError("error.username.required"));
        }

        if ((password == null) || (password.length() < 1)) {
            errors.add("password", new ActionError("error.password.required"));
        }

        return errors;
    }

    /**
     * Getter for property username.
     *
     * @return Value of property username.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Setter for property username.
     *
     * @param username New value of property username.
     */
    public void setUsername(final String username) {
        if (username != null) {
            this.username = username.trim();
        } else {
            this.username = null;
        }
    }

    /**
     * Getter for property password.
     *
     * @return Value of property password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Setter for property password.
     *
     * @param password New value of property password.
     */
    public void setPassword(final String password) {
        if (password != null) {
            this.password = password.trim();
        } else {
            this.password = null;
        }
    }

    /**
     * Getter for property submit.
     *
     * @return Value of property submit.
     */
    public String getAction() {
        return this.action;
    }

    /**
     * Setter for property action.
     *
     * @param action New value of property action.
     */
    public void setAction(final String action) {
        if (action != null) {
            this.action = action.trim();
        } else {
            this.action = null;
        }
    }
}