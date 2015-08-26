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

package edu.ufl.osg.webmail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;


/**
 * JavaMail Authenticator that gets the username and password from our User
 * object.
 * @author sandymac
 * @version $Revision: 1.2 $
 */
public class WebMailAuthenticator extends Authenticator {
    /** Holds value of property user. */
    private User user;

    /**
     * Instiaciates a new Autenticator for the specified user.
     *
     * @param user User object for which the username and password will be
     *        fetched from.
     */
    public WebMailAuthenticator(final User user) {
        setUser(user);
    }

    /**
     * Called when password authentication is needed.
     *
     * @return The PasswordAuthentication collected from the user, or null if
     *         none is provided.
     */
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user.getUsername(), user.getPassword());
    }

    /**
     * Getter for property user.
     *
     * @return Value of property user.
     */
    public User getUser() {
        return this.user;
    }

    /**
     * Setter for property user.
     *
     * @param user New value of property user.
     */
    public void setUser(final User user) {
        this.user = user;
    }
}