/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002-2004 William A. McArthur, Jr.
 * Copyright (C) 2003, 2004 The Open Systems Group / University of Florida
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

package edu.ufl.osg.webmail.imap;

import edu.ufl.osg.webmail.SessionProvider;
import edu.ufl.osg.webmail.User;
import edu.ufl.osg.webmail.WebMailAuthenticator;

import javax.mail.Authenticator;
import javax.mail.Session;
import javax.servlet.ServletRequest;
import java.util.Properties;


/**
 * @author sandymac
 * @version $Revision: 1.5 $
 */
public class IMAPSessionProvider implements SessionProvider {
    private final Properties props;
    private final String domain;

    /**
     * Creates a new instance of IMAPSessionProvider.
     */
    public IMAPSessionProvider(final Properties props, final String domain) {
        this.props = props;
        this.domain = domain;
    }

    public User getUser(final String username, final String password, final ServletRequest request) {
        //          User user = new IMAPUser(username, password);
        final User user = new User(username, password);
        user.setEmail(user.getUsername() + '@' + getDomain());
        // other user properties will get populated lazily
        return user;
    }

    public Authenticator getAuthenticator(final User user) {
        return new WebMailAuthenticator(user);
    }

    public Session getSession(final Authenticator authenticator) {
        return Session.getInstance(props, authenticator);
    }

    /**
     * Getter for property domain.
     *
     * @return Value of property domain.
     */
    private String getDomain() {
        return this.domain;
    }
}
