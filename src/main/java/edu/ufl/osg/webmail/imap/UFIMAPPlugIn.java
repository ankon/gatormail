/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2004 The Open Systems Group / University of Florida
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

import edu.ufl.osg.webmail.Constants;
import edu.ufl.osg.webmail.SessionProvider;
import edu.ufl.osg.webmail.data.DAOFactory;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;

import javax.servlet.ServletContext;
import java.util.Properties;

/**
 * IMAP Plugin for UF.
 *
 * <p>NOTE: This requires a JavaMail 1.3.2 or above.
 *
 * @author sandymac
 * @version $Revision: 1.2 $
 * @since Aug 4, 2004
 */
public class UFIMAPPlugIn implements PlugIn {

    protected static final String STORE_PROTOCOL_KEY = "mail.store.protocol";
    private static final String TRANSPORT_PROTOCOL_KEY = "mail.transport.protocol";
    private static final String SMTP_HOST_KEY = "mail.smtp.host";
    private static final String SMTP_PORT_KEY = "mail.smtp.port";
    private final String HOST_KEY;
    private final String PORT_KEY;
    private final String CONNECTION_TIMEOUT_KEY;
    private final String CONNECTION_POOL_SIZE_KEY;
    private final String TIMEOUT_KEY;
    private final String SASL_ENABLE_KEY;
    private final String SASL_AUTH_KEY;

    private static final String AUTH_PLAIN_AUTHENTICATION = "mail.imap.auth.plain.authentication";
    private static final String AUTH_PLAIN_PASSWORD = "mail.imap.auth.plain.password";

    /**
     * The {@link ActionServlet} owning this application.
     */
    private ActionServlet servlet = null;

    /**
     * The application configuration for our owning module.
     */
    private ModuleConfig config = null;
    protected Properties props = new Properties(System.getProperties());
    protected final String protocol;

    public UFIMAPPlugIn() {
        this("imap");
    }

    public UFIMAPPlugIn(final String protocol) {
        this.protocol = protocol;

        HOST_KEY = "mail." + protocol + ".host";
        PORT_KEY = "mail." + protocol + ".port";
        CONNECTION_TIMEOUT_KEY = "mail." + protocol + ".connectiontimeout";
        CONNECTION_POOL_SIZE_KEY = "mail." + protocol + ".connectionpoolsize";
        TIMEOUT_KEY = "mail." + protocol + ".timeout";
        SASL_ENABLE_KEY = "mail." + protocol + ".sasl.enable";
        SASL_AUTH_KEY = "mail." + protocol + ".sasl.authorizationid";

        props.setProperty(STORE_PROTOCOL_KEY, protocol);
        props.setProperty(TRANSPORT_PROTOCOL_KEY, "smtp");
    }

    /**
     * Receive notification that the specified module is being started up.
     *
     * @param servlet ActionServlet that is managing all the modules in this
     *                web application
     * @param config  ModuleConfig for the module with which this plug-in is
     *                associated
     */
    public void init(final ActionServlet servlet, final ModuleConfig config) {
        // Remember our associated configuration and servlet
        this.config = config;
        this.servlet = servlet;
        final ServletContext servletContext = servlet.getServletContext();

        final String authentication = servletContext.getInitParameter(AUTH_PLAIN_AUTHENTICATION);
        final String password = servletContext.getInitParameter(AUTH_PLAIN_PASSWORD);

        final SessionProvider sessionProvider = createSessionProvider(authentication, password);
        DAOFactory.getInstance().setSessionProvider(sessionProvider);
        servletContext.setAttribute(Constants.MAIL_SESSION_PROVIDER, sessionProvider);
    }

    protected SessionProvider createSessionProvider(final String authentication, final String password) {
        return new UFIMAPSessionProvider(protocol, props, authentication, password);
    }

    public void destroy() {
        servlet = null;
        config = null;
    }

    /**
     * Setter for property host.
     *
     * @param host New value of property host.
     */
    public void setHost(final String host) {
        setIMAPHost(host);
        setSMTPHost(host);
    }

    /**
     * Getter for property IMAPHost.
     *
     * @return Value of property IMAPHost.
     */
    public String getIMAPHost() {
        return props.getProperty(HOST_KEY);
    }

    /**
     * Setter for property IMAPHost.
     *
     * @param IMAPHost New value of property IMAPHost.
     */
    public void setIMAPHost(final String IMAPHost) {
        props.setProperty(HOST_KEY, IMAPHost);
    }

    /**
     * Getter for property IMAPPort.
     *
     * @return Value of property IMAPPort.
     */
    public String getIMAPPort() {
        return props.getProperty(PORT_KEY);
    }

    /**
     * Setter for property IMAPPort.
     *
     * @param IMAPPort New value of property IMAPPort.
     */
    public void setIMAPPort(final String IMAPPort) {
        props.setProperty(HOST_KEY, IMAPPort);
    }

    /**
     * Getter for property SMTPHost.
     *
     * @return Value of property SMTPHost.
     */
    public String getSMTPHost() {
        return props.getProperty(SMTP_HOST_KEY);
    }

    /**
     * Setter for property SMTPHost.
     *
     * @param SMTPHost New value of property SMTPHost.
     */
    public void setSMTPHost(final String SMTPHost) {
        props.setProperty(SMTP_HOST_KEY, SMTPHost);
    }

    /**
     * Getter for property SMTPPort.
     *
     * @return Value of property SMTPPort.
     */
    public String getSMTPPort() {
        return props.getProperty(SMTP_PORT_KEY);
    }

    /**
     * Setter for property SMTPPort.
     *
     * @param SMTPPort New value of property SMTPPort.
     */
    public void setSMTPPort(final String SMTPPort) {
        props.setProperty(SMTP_HOST_KEY, SMTPPort);
    }

    /**
     * get socket connection timeout value in milliseconds.
     */
    public String getConnectionTimeout() {
        return props.getProperty(CONNECTION_TIMEOUT_KEY);
    }

    /**
     * set socket connection timeout value in milliseconds.
     */
    public void setConnectionTimeout(final String connectionTimeout) {
        props.setProperty(CONNECTION_TIMEOUT_KEY, connectionTimeout);
    }

    public String getConnectionPoolSize() {
        return props.getProperty(CONNECTION_POOL_SIZE_KEY);
    }

    public void setConnectionPoolSize(final String connectionPoolSize) {
        props.setProperty(CONNECTION_POOL_SIZE_KEY, connectionPoolSize);
    }

    /**
     * get socket I/O timeout value in milliseconds
     */
    public String getTimeout() {
        return props.getProperty(TIMEOUT_KEY);
    }

    /**
     * set socket I/O timeout value in milliseconds
     */
    public void setTimeout(final String timeout) {
        props.setProperty(TIMEOUT_KEY, timeout);
    }

}
