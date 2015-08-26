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


package edu.ufl.osg.webmail.prefs;

import edu.ufl.osg.webmail.User;

import javax.servlet.http.HttpSession;
import java.util.Properties;

/**
 * University of Florida specific {@link DBPrefsProvider}. This class just
 * overrides the {@link #userKey} method to return the UFID instead of the
 * username.
 * 
 * @author sandymac
 * @since  Sep 4, 2003 6:15:17 PM
 * @version $Revision: 1.2 $
 */
public class UFDBPrefsProvider extends DBPrefsProvider {

    public UFDBPrefsProvider(final ConnectionProvider cp) {
        super(cp);
    }

    /**
     * Return the key for table rows associated with this <code>user</code>.
     * This returns the user's UFID.
     *
     * @param  user                the {@link User} we are accessing preferences
     *                             for.
     * @return                     key for rows in the table associate with the
     *                             <code>user</code>.
     * @throws ClassCastException  If a specifc subsclass of {@link User} is
     *                             expected.
     */
    protected String userKey(final User user) throws ClassCastException {
        // TODO Make this only work for UFUser when that gets created.
        return user.getPermId();
    }

    /**
     * Populate the "user.name" or the "compose.signature" properties if either are null.
     *
     * @param prefs               the preferences to be modified.
     * @param user                The user who's preferences we are looking up.
     * @param session             The user's {@link HttpSession} incase the
     *                            provider wants to access it.
     * @see   #getPreferences
     */
    protected void populatePreferences(final Properties prefs, final User user, final HttpSession session) {
        super.populatePreferences(prefs, user, session);

        // TODO: When User is refactored to a User and UFUser update these.

        // Populate user name
        final String username = prefs.getProperty("user.name");
        if (username == null) {
            prefs.setProperty("user.name", user.getDisplayName());
        }

        final String signature = prefs.getProperty("compose.signature");
        if (signature == null) {
            prefs.setProperty("compose.signature", user.getSignature());
        }
    }
}
