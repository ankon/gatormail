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

import org.apache.struts.action.PlugIn;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.ModuleConfig;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.mail.internet.MimeUtility;
import javax.mail.MessagingException;

import edu.ufl.osg.webmail.Constants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Sets up a databased backed preference presistance engine.
 *
 * @author sandymac
 * @since Sep 16, 2003 5:56:15 PM
 * @version $Revision: 1.4 $
 */
public class DBPrefsPlugIn implements PlugIn, DBPrefsProvider.ConnectionProvider {
    private static final Logger logger = Logger.getLogger(DBPrefsPlugIn.class.getName());

    private ActionServlet servlet;
    private ModuleConfig config;

    private String contextName = null;
    private String jdbcDriver = null;
    private String jdbcUrl = null;
    private DataSource dataSource;

    public void init(final ActionServlet servlet, final ModuleConfig config) throws ServletException {
        if (getContextName() == null && getJdbcDriver() == null && getJdbcUrl() == null) {
            final ServletException se = new ServletException("contextName or jdbcDriver/jdbcUrl must be set for " + this.getClass().getName());
            logger.error(se.getMessage(), se);
            throw se;
        } else if (getContextName() != null && (getJdbcDriver() != null || getJdbcUrl() != null)) {
            final ServletException se = new ServletException("Only one of contextName or jdbcDriver/jdbcUrl can be set for " + this.getClass().getName());
            logger.error(se.getMessage(), se);
            throw se;
        }

        this.servlet = servlet;
        this.config = config;

        if (getContextName() != null) {
            initializeJndiDriver();
        } else {
            initializeJdbcDriver();
        }

        final DBPrefsProvider dbPrefs = getDBPrefsProvider();

        final PreferencesProvider prefs = dbPrefs;

        servlet.getServletContext().setAttribute(Constants.PREFERENCES_PROVIDER, prefs);
    }

    protected DBPrefsProvider getDBPrefsProvider() {
        return new DBPrefsProvider(this);
    }

    /**
     * Loads the JDBC driver class for the {@link DriverManager}.
     *
     * @throws ServletException    wraps a {@link Throwable} is thrown by either
     *                             {@link Class#forName(String)} or {@link
     *                             Class#newInstance()}.
     */
    private void initializeJdbcDriver() throws ServletException {
        logger.debug("initializeJdbcDriver()");
        if (getJdbcDriver() == null) {
            final NullPointerException npe = new NullPointerException("jdbcDriver cannot be null if not using JNDI mappings.");
            throw new ServletException(npe.getMessage(), npe);
        }
        if (getJdbcUrl() == null) {
            final NullPointerException npe = new NullPointerException("jdbcUrl cannot be null if not using JNDI mappings.");
            throw new ServletException(npe.getMessage(), npe);
        }

        Class clazz = null;
        try {
            clazz = Class.forName(getJdbcDriver());
        } catch (ClassNotFoundException cnfe) {
            throw new ServletException(getJdbcDriver() + " cannot be located.", cnfe);
        } catch (ExceptionInInitializerError eiie) {
            throw new ServletException("Exception in a static initalizer in: " + getJdbcDriver(), eiie);
        } catch (LinkageError le) {
            throw new ServletException("Error linking: " + getJdbcDriver(), le);
        }

        try {
            clazz.newInstance();
        } catch (InstantiationException ie) {
            throw new ServletException("Problem instantiation: " + getJdbcDriver(), ie);
        } catch (IllegalAccessException iae) {
            throw new ServletException(getJdbcDriver() + " or it's initializer is not accessable.", iae);
        } catch (ExceptionInInitializerError eiie) {
            throw new ServletException("Exception in a static initalizer in: " + getJdbcDriver(), eiie);
        } catch (SecurityException se) {
            throw new ServletException("Security manager prevents instantiation of: " + getJdbcDriver(), se);
        }
        logger.debug("~initializeJdbcDriver()");
    }

    /**
     * Loads the {@link DataSource} from a JNDI mapping.
     *
     * @throws ServletException    if a {@link NamingException} occurs.
     */
    private void initializeJndiDriver() throws ServletException {
        logger.debug("initializeJndiDriver()");
        if (getContextName() == null) {
            final NullPointerException npe = new NullPointerException("contextName cannot be if using JNDI mappings.");
            throw new ServletException(npe.getMessage(), npe);
        }

        try {
            final Context ctx = new InitialContext();
            logger.debug("getContextName(): " + getContextName());
            dataSource = (DataSource)ctx.lookup(getContextName());
        } catch (NamingException ne) {
            logger.error("Problem looking up data source: " + getContextName(), ne);
            throw new ServletException("Problem looking up data source: " + getContextName(), ne);
        }
        logger.debug("~initializeJndiDriver()");
    }

    public void destroy() {
        servlet.getServletContext().removeAttribute(Constants.PREFERENCES_PROVIDER);
        config = null;
    }

    public Connection getConnection() throws SQLException {
        logger.debug("getConnection() Thread: " + Thread.currentThread().getName());
        if (dataSource != null) {
            return dataSource.getConnection();
        } else {
            return DriverManager.getConnection(getJdbcUrl());
        }
    }

    public String getContextName() {
        return contextName;
    }

    public void setContextName(final String contextName) {
        this.contextName = contextName;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public void setJdbcDriver(final String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(final String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }
}