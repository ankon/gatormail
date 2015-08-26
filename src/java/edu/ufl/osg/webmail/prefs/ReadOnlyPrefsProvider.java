/*
* This file is part of GatorMail, a servlet based webmail.
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
import java.io.InputStream;
import java.io.IOException;

/**
 * Read only preferences read from a property file.
 *
 * @author sandymac
 * @since Aug 6, 2003 2:30:22 PM
 * @version $Revision: 1.3 $
 */
public class ReadOnlyPrefsProvider implements PreferencesProvider {

    private Properties readOnlyPrefs;

    /**
     * Loads a set of properties once.
     *
     * @param  prefsFileName       the name of the resources to be loaded.
     * @throws IOException         If there is a problem loading the properties
     *                             file.
     */
    public ReadOnlyPrefsProvider(final String prefsFileName) throws IOException {
        final Properties props = new Properties();

        final InputStream in = ReadOnlyPrefsProvider.class.getResourceAsStream(prefsFileName);
        props.load(in);

        readOnlyPrefs = props;
    }

    /**
     * Returns {@link Properties} for a specific {@link User}. This method may be
     * called frequently so performance should be a consideration.
     *
     * @param  user                The user who's preferences we are looking up.
     * @param  session             The user's {@link HttpSession} incase the
     *                             provider wants to access it.
     * @return                     Ths user's preferences represented as {@link
     *                             Properties}.
     */
    public Properties getPreferences(final User user, final HttpSession session) {
        return new Properties(readOnlyPrefs);
    }
}
