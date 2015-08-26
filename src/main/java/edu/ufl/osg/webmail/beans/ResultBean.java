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

package edu.ufl.osg.webmail.beans;

import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * Bean for displaying the result(s) of an action.
 *
 * @author drakee
 * @version $Revision: 1.2 $
 */
public class ResultBean {
    private static final Logger logger = Logger.getLogger(ResultBean.class.getName());

    private String message;

    /**
     * Creates a new instance of ResultBean
     */
    public ResultBean() {
    }

    /**
     * Creates a new instance of ResultBean
     * with the result message initialized
     */
    public ResultBean(final String message) {
        setMessage(message);
    }

    /**
     * Creates a new instance of ResultBean
     * with the result message initialized
     */
    public ResultBean(final String message, final String arg1) {
        setMessage(message, arg1);
    }

    /**
     * Creates a new instance of ResultBean
     * with the result message initialized
     */
    public ResultBean(final String message, final String arg1, final String arg2) {
        setMessage(message, arg1, arg2);
    }

    /**
     * Getter for property message.
     *
     * @return Value of property message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter for property message.
     *
     * @param message New value of property message.
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * Setter for property message.
     *
     * @param message New value of property message.
     */
    public void setMessage(final String message, final String arg1) {
        this.message = MessageFormat.format(message, new String[]{arg1});
    }

    /**
     * Setter for property message.
     *
     * @param message New value of property message.
     */
    public void setMessage(final String message, final String arg1, final String arg2) {
        this.message = MessageFormat.format(message, new String[]{arg1, arg2});
    }

    public String toString() {
        return getMessage();
    }
}
