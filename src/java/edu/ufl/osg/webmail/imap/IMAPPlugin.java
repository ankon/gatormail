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

import edu.ufl.osg.webmail.Constants;
import edu.ufl.osg.webmail.SessionProvider;
import edu.ufl.osg.webmail.data.DAOFactory;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;

import java.util.Properties;


/**
 * @author sandymac
 * @version $Revision: 1.7 $
 */
public class IMAPPlugin implements PlugIn {
    private static final String STORE_PROTOCOL_KEY = "mail.store.protocol";
    private static final String TRANSPORT_PROTOCOL_KEY = "mail.transport.protocol";
    private static final String IMAP_HOST_KEY = "mail.imap.host";
    private static final String IMAP_PORT_KEY = "mail.imap.port";
    private static final String SMTP_HOST_KEY = "mail.smtp.host";
    private static final String SMTP_PORT_KEY = "mail.smtp.port";
    private static final String CONNECTION_TIMEOUT_KEY = "mail.imap.connectiontimeout";
    private static final String CONNECTION_POOL_SIZE_KEY = "mail.imap.connectionpoolsize";
    private static final String TIMEOUT_KEY = "mail.imap.timeout";

    /** The {@link ActionServlet} owning this application. */
    private ActionServlet servlet = null;

    /** The application configuration for our owning module. */
    private ModuleConfig config = null;
    private Properties props = new Properties(System.getProperties());
    private String domain = null;

    /**
     * Creates a new instance of Provider
     */
    public IMAPPlugin() {
        props.setProperty(STORE_PROTOCOL_KEY, "imap");
        props.setProperty(TRANSPORT_PROTOCOL_KEY, "smtp");
    }

    /**
     * Receive notification that the specified module is being started up.
     *
     * @param servlet ActionServlet that is managing all the modules in this
     *        web application
     * @param config ModuleConfig for the module with which this plug-in is
     *        associated
     */
    public void init(final ActionServlet servlet, final ModuleConfig config) {
        // Remember our associated configuration and servlet
        this.config = config;
        this.servlet = servlet;

        if (null == servlet.getServletContext().getInitParameter("gatormail.do.not.use.ssl")) {
            props.setProperty("mail.imap.port", "993");
            props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.imap.socketFactory.port", "993");
            props.setProperty("mail.imap.socketFactory.fallback", "false");
        }

        final String nodeImapHost = servlet.getServletContext().getInitParameter(IMAP_HOST_KEY);
        if (nodeImapHost != null) {
            servlet.log("IMAP Host Override in effect. was: " + getIMAPHost() + ", now: " + nodeImapHost);
            setIMAPHost(nodeImapHost);
        }

        // set global session provider
        final SessionProvider sessionProvider = new IMAPSessionProvider(props, domain);
        DAOFactory.getInstance().setSessionProvider(sessionProvider);
        servlet.getServletContext().setAttribute(Constants.MAIL_SESSION_PROVIDER, sessionProvider);
    }

    public void destroy() {
        // Nothing to do
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
     * Getter for property Domain.
     *
     * @return Value of property Domain.
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Setter for property Domain.
     *
     * @param domain New value of property Domain.
     */
    public void setDomain(final String domain) {
        this.domain = domain;
    }

    /**
     * Getter for property IMAPHost.
     *
     * @return Value of property IMAPHost.
     */
    public String getIMAPHost() {
        return props.getProperty(IMAP_HOST_KEY);
    }

    /**
     * Setter for property IMAPHost.
     *
     * @param IMAPHost New value of property IMAPHost.
     */
    public void setIMAPHost(final String IMAPHost) {
        props.setProperty(IMAP_HOST_KEY, IMAPHost);
    }

    /**
     * Getter for property IMAPPort.
     *
     * @return Value of property IMAPPort.
     */
    public String getIMAPPort() {
        return props.getProperty(IMAP_PORT_KEY);
    }

    /**
     * Setter for property IMAPPort.
     *
     * @param IMAPPort New value of property IMAPPort.
     */
    public void setIMAPPort(final String IMAPPort) {
        props.setProperty(IMAP_HOST_KEY, IMAPPort);
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
