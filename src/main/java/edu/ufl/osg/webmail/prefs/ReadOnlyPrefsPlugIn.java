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

import org.apache.struts.action.PlugIn;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.ModuleConfig;

import javax.servlet.ServletException;

import edu.ufl.osg.webmail.Constants;

import java.io.IOException;

/**
 * Provides a read-only set of preferences. This is not really intended for
 * production use.
 *
 * @author sandymac
 * @since  Aug 5, 2003 3:12:38 PM
 * @version $Revision: 1.2 $
 */
public class ReadOnlyPrefsPlugIn implements PlugIn {
    private ActionServlet servlet;
    private ModuleConfig config;
    private String prefsFileName;

    public void init(final ActionServlet servlet, final ModuleConfig config) throws ServletException {
        this.servlet = servlet;
        this.config = config;

        PreferencesProvider prefs = null;
        try {
            prefs = new ReadOnlyPrefsProvider(getPrefsFileName());
        } catch (IOException ioe) {
            throw new ServletException("Problem loading properties from: " + getPrefsFileName(), ioe);
        }

        servlet.getServletContext().setAttribute(Constants.PREFERENCES_PROVIDER, prefs);
    }

    public void destroy() {
        servlet.getServletContext().removeAttribute(Constants.PREFERENCES_PROVIDER);
    }

    public String getPrefsFileName() {
        return prefsFileName;
    }

    public void setPrefsFileName(final String prefsFileName) {
        this.prefsFileName = prefsFileName;
    }
}
