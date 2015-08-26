/*
* This file is part of GatorMail, a servlet based webmail.
* Copyright (C) 2003-2004 The Open Systems Group / University of Florida
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
 * Sets up a preferences provider that doesn't presist changes.
 *
 * @author sandymac
 * @since Aug 28, 2003 3:27:42 PM
 * @version $Revision: 1.3 $
 */
public class TransientPrefsProvider implements PreferencesProvider {
    /**
     * Returns {@link Properties} for a specific {@link User}. This method may
     * be called frequently so performance should be a consideration.
     *
     * @param  user                The user who's preferences we are looking up.
     * @param  session             The user's {@link HttpSession} incase the
     *                             provider wants to access it.
     * @return                     Ths user's preferences represented as {@link
     *                             Properties}.
     */
    public Properties getPreferences(final User user, final HttpSession session) {
        Properties prefs = (Properties)session.getAttribute("userPrefs");
        if (prefs == null) {
            prefs = new Properties();
            prefs.setProperty("user.name", user.getDisplayName());
            prefs.setProperty("compose.signature", user.getSignature());
            session.setAttribute("userPrefs", prefs);
        }
        return prefs;
    }
}
