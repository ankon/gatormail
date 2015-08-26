/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002-2004 William A. McArthur, Jr.
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

package edu.ufl.osg.webmail;

import edu.ufl.osg.webmail.data.DAOFactory;
import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;

import javax.mail.Authenticator;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * The User interface.
 *
 * XXX: Changed this to a class - Drake
 * TODO: Make this an interface again. - Sandy
 *
 * @author sandymac
 * @author drakee
 * @version $Revision: 1.6 $
 */
public class User implements Serializable, HttpSessionBindingListener {
    private static final Logger logger = Logger.getLogger(User.class.getName());

    private String username;
    private String password;
    private String email;
    private String displayName;
    private String permId;
    private String signature;
    // JavaMail objects are not Serializable
    private transient Session mailSession = null;
    private transient Store mailStore = null;

    private long boundTime = 0;
    private volatile List recentAcitivity = new LinkedList();

    /**
     * Creates a new instance of IMAPUser
     */
    public User(final String username, final String password) {
        setUsername(username);
        setPassword(password);
    }

    /**
     * Getter for property password.
     *
     * @return Value of property password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter for property password.
     *
     * @param password New value of property password.
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Getter for property username.
     *
     * @return Value of property username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter for property username.
     *
     * @param username New value of property username.
     */
    protected void setUsername(final String username) {
        this.username = username;
    }

    /**
     * Getter for property email.
     *
     * @return Value of property email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setter for property email.
     *
     * @param email New value of property email.
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * Getter for property displayName.
     *
     * @return Value of property displayName.
     * @deprecated                     Use PreferencesProvider.getPreferences(User).getProperty("user.name"));
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Setter for property displayName.
     *
     * @param displayName New value of property displayName.
     * @deprecated                     Use PreferencesProvider.getPreferences(User).setProperty("user.name", "..."));
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Getter for property permId.
     *
     * @return Value of property permId.
     */
    public String getPermId() {
        return permId;
    }

    /**
     * Setter for property permId.
     *
     * @param permId New value of property permId.
     */
    public void setPermId(final String permId) {
        this.permId = permId;
    }

    /**
     * Getter for property signature.
     *
     * @return                         Value of property signature.
     * @deprecated                     Use PreferencesProvider.getPreferences(User).getProperty("compose.signature"));
     */
    public String getSignature() {
        if (signature == null) {
            // fancy signature - as per specs, this not user-configurable
            setSignature("-- \r\n" + getDisplayName());
        }
        return signature;
    }

    /**
     * Setter for property signature.
     *
     * @param signature New value of property signature.
     * @deprecated                     Use PreferencesProvider.getPreferences(User).setProperty("compose.signature", "..."));
     */
    public void setSignature(final String signature) {
        this.signature = signature;
    }

    /**
     * Getter for property mailSession.
     *
     * @return mailSession object
     */
    public Session getMailSession() {
	// mailSession may be null if session persistance had to be used, since
	// it is a transient member.
	if (mailSession == null) {
	    final SessionProvider sessionProvider = DAOFactory.getInstance().getSessionProvider();
	    final Authenticator auth = sessionProvider.getAuthenticator(this);
	    mailSession = sessionProvider.getSession(auth);
	}
        return mailSession;
    }

    /**
     * Getter for property mailStore.
     *
     * @return mailStore object
     */
    public Store getMailStore() throws NoSuchProviderException {
	// mailStore may be null if session persistance had to be used, since
	// it is a transient member.
	if (mailStore == null) {
	    mailStore = getMailSession().getStore();
	}
        return mailStore;
    }

    ////////////////////////////////////////////////////////////
    // Object methods
    ////////////////////////////////////////////////////////////
    public String toString() {
        return username;
    }

    public boolean DISABLEequals(final Object obj) {
        if (obj instanceof User) {
            final User user = (User)obj;

            return username.equals(user.getUsername()) && password.equals(user.getPassword());
        } else {
            return false;
        }
    }

    public int DISABLEhashCode() {
        return username.hashCode() * password.hashCode();
    }

    /**
     * Notifies the object that it is being bound to a session and identifies
     * the session.
     *
     * @param event               the event that identifies the session.
     * @see   javax.servlet.http.HttpSessionBindingListener
     */
    public void valueBound(final HttpSessionBindingEvent event) {
        Util.addActiveSession(this);
        boundTime = System.currentTimeMillis();
        logger.debug("User " + this + " bound in " + Runtime.getRuntime().hashCode());
    }

    /**
     * Notifies the object that it is being unbound from a session and
     * identifies the session.
     *
     * @param event               the event that identifies the session.
     * @see   javax.servlet.http.HttpSessionBindingListener
     */
    public void valueUnbound(final HttpSessionBindingEvent event) {
        Util.removeActiveSession(this);
        logger.debug("User " + this + " unbound after " + (System.currentTimeMillis() - boundTime) + "ms in " + Runtime.getRuntime().hashCode());
    }

    public synchronized void updateActivity(final HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null) {
            queryString = "";
        } else {
            queryString = "?" + queryString;
        }
        StringBuffer requestURL = request.getRequestURL();
        if (requestURL == null) {
            requestURL = new StringBuffer();
        }
        String fullRequestURL = requestURL.append(queryString).toString();
        final Activity activity = new Activity(fullRequestURL);
        recentAcitivity.add(activity);

        // Trim if needed
        if (recentAcitivity.size() > 5) {
            try {
                recentAcitivity.remove(0);
            } catch (IndexOutOfBoundsException e) {
                // ignore
            }
        }
    }

    public long getBoundTime() {
        return boundTime;
    }

    public List getRecentAcitivity() {
        return recentAcitivity;
    }

    public static class Activity implements Serializable {
        private String request;
        private final long time;

        private Activity(final String request) {
            time = System.currentTimeMillis();
            setRequest(request);
        }

        public String getRequest() {
            return request;
        }

        private void setRequest(final String request) {
            this.request = request.intern();
        }

        public long getTime() {
            return time;
        }
    }
}
