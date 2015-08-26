/*
* This file is part of GatorMail, a servlet based webmail.
* Copyright (C) 2002, 2003 William A. McArthur, Jr.
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
import org.apache.log4j.Logger;

import javax.servlet.http.HttpSession;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

/**
 * Database backed {@link PreferencesProvider} that caches user's preferences.
 *
 * @author sandymac
 * @since Sep 4, 2003 4:57:09 PM
 * @version $Revision: 1.6 $
 */
public class DBPrefsProvider implements PreferencesProvider {
    private static final Logger logger = Logger.getLogger(DBPrefsProvider.class.getName());

    /**
     * {@link HttpSession} key for the {@link Long} when the current user's
     * preferences was last updated.
     */
    private static final String PREFERENCE_UPDATE_KEY = DBPrefsProvider.class.getClass().getName() + "UpdateKey";

    private static final Map PREFS_CACHE = new WeakHashMap();

    /**
     * This is used to hold on to the user's {@link HttpSession} so the {@link
     * DBProperties} can have access to it without changing it's interface.
     * {@link HttpSession}s put into this TheadLocal should be wrapped behind a
     * {@link WeakReference} to prevent a memory leak.
     */
    private static final ThreadLocal HTTP_SESSIONS = new InheritableThreadLocal();

    /**
     *
     */
    private static final ThreadLocal USERS = new InheritableThreadLocal();

    private final ConnectionProvider connectionProvider;

    public DBPrefsProvider(final ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    /**
     * Returns {@link Properties} for a specific {@link User}. <b>Note</b>: the
     * returned {@link Properties} <i>MUST NOT</i> be cached in any which way
     * beyond the scope of the current HTTP request.
     *
     * @param  user                The user who's preferences we are looking up.
     * @param  session             The user's {@link HttpSession} incase the
     *                             provider wants to access it.
     * @return                     Ths user's preferences represented as {@link
     *                             Properties}.
     */
    public final Properties getPreferences(final User user, final HttpSession session) {
        Properties prefs;

        HTTP_SESSIONS.set(new WeakReference(session));
        USERS.set(new WeakReference(user));

        prefs = fetchCachedPerferencesIfCurrent(user, session);

        if (prefs == null) {
            prefs = loadUsersPreferences(user);
            final CacheEntry entry = new CacheEntry(prefs);
            PREFS_CACHE.put(user, entry);
        }

        populatePreferences(prefs, user, session);
        return prefs;
    }

    private Properties loadUsersPreferences(final User user) {
        final Properties prefs = new DBProperties(userKey(user));
        return prefs;
    }

    /**
     * Reteives cached prefences or null if no acceptable cached perferences are
     * found. If the user has cached preferences but they are not current then
     * <code>null</code> is returned.
     *
     * @param  user
     * @param  session
     * @return                     The user's perferences or <code>null</code>
     *                             if not found.
     */
    private static Properties fetchCachedPerferencesIfCurrent(final User user, final HttpSession session) {
        Properties prefs = null;
        final CacheEntry entry = (CacheEntry)PREFS_CACHE.get(user);
        if (entry != null) {
            final Long updated = (Long)session.getAttribute(PREFERENCE_UPDATE_KEY);

            if (updated == null || updated.longValue() <= entry.getUpdated()) {
                prefs = entry.getPrefs();
            }
        }
        return prefs;
    }

    /**
     * Return the key for table rows associated with this <code>user</code>.
     * Databases that key off of something other than the user name should
     * override this method.
     *
     * @param  user                the {@link User} we are accessing preferences
     *                             for.
     * @return                     key for rows in the table associate with the
     *                             <code>user</code>.
     * @throws ClassCastException  If a specifc subsclass of {@link User} is
     *                             expected.
     */
    protected String userKey(final User user) throws ClassCastException {
        return user.getUsername();
    }

    /**
     * Gives sub classes a chance to populate the preferences with any values
     * before it's returned from {@link #getPreferences(User, HttpSession)}.
     *
     * @param prefs               the preferences to be modified.
     * @param user                The user who's preferences we are looking up.
     * @param session             The user's {@link HttpSession} incase the
     *                            provider wants to access it.
     * @see   #getPreferences
     */
    protected void populatePreferences(final Properties prefs, final User user, final HttpSession session) {
        // Nothing
    }

    /**
     * Tracks when preferences were loaded.
     */
    private static class CacheEntry {
        private Properties prefs;
        private long updated;

        private CacheEntry(final Properties prefs) {
            this(prefs, System.currentTimeMillis());
        }

        private CacheEntry(final Properties prefs, final long updated) {
            if (prefs == null) {
                throw new NullPointerException("prefs may not be null.");
            }
            this.prefs = prefs;
            this.updated = updated;
        }

        public Properties getPrefs() {
            return prefs;
        }

        public long getUpdated() {
            return updated;
        }

        public void setUpdated(final long updated) {
            this.updated = updated;
        }
    }

    private class DBProperties extends Properties {
        private final String userKey;

        /**
         * Creates an property list filled with the user's previously stored
         * values.
         *
         * @param userKey             the key for this user.
         */
        public DBProperties(final String userKey) {
            this.userKey = userKey;

            // TODO Fetch properties
            Connection connection = null;
            try {
                connection = connectionProvider.getConnection();

                PreparedStatement ps = null;
                try {
                    if (connection.getMetaData().getDatabaseProductName().equals("MySQL")) { // Because MySQL doesn't like the key column name.
                        ps = connection.prepareStatement("SELECT preferences.key, preferences.entry " + "FROM preferences " + "WHERE userid = ?");
                    } else {
                        ps = connection.prepareStatement("SELECT key, entry " + "FROM preferences " + "WHERE userid = ?");
                    }
                    ps.setString(1, userKey);

                } catch (SQLException se) {
                    logger.error("Problem creating a prepared statment.", se);
                    if (ps != null) ps.cancel();
                    return;
                }

                ResultSet rs = null;
                try {
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String key = rs.getString(1);
                        final String entry = rs.getString(2);
                        super.setProperty(key, entry);
                    }

                } catch (SQLException e) {
                    logger.error("Problem with the result set.", e);
                    if (rs != null) rs.close();
                    return;
                }
            } catch (SQLException e) {
                logger.error("Problem getting a Connection", e);
                return;

            } finally {
                try {
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    logger.error("Problem closing the Connection", e);
                }
            }
        }

        /**
         * Calls the <tt>Hashtable</tt> method <code>put</code>. Provided for
         * parallelism with the <tt>getProperty</tt> method. Enforces use of
         * strings for property keys and values. The value returned is the
         * result of the <tt>Hashtable</tt> call to <code>put</code>.
         *
         * @param key the key to be placed into this property list.
         * @param value the value corresponding to <tt>key</tt>.
         * @return     the previous value of the specified key in this property
         *             list, or <code>null</code> if it did not have one.
         */
        public synchronized Object setProperty(final String key, final String value) throws RuntimeException {
            deleteKey(key);
            if (value != null) {
                insertKeyValue(key, value);
            }


            // Update cache and syncronization objects
            final Long updateTime = new Long(System.currentTimeMillis());

            final User user = getUser();
            final CacheEntry entry = (CacheEntry)PREFS_CACHE.get(user);
            entry.setUpdated(updateTime.longValue());

            final HttpSession session = getHttpSession();
            if (session != null) {
                session.setAttribute(PREFERENCE_UPDATE_KEY, updateTime);
            }

            return super.setProperty(key, value);
        }

        /**
         * Removes the key (and its corresponding value) from this hashtable.
         * This method does nothing if the key is not in the hashtable.
         *
         * @param  key                  the key that needs to be removed.
         * @return                      the value to which the key had been
         *                              mapped in this hashtable, or
         *                              <code>null</code> if the key did not
         *                              have a mapping.
         * @throws NullPointerException if the key is <code>null</code>.
         */
        public synchronized Object remove(final Object key) {
            deleteKey(key.toString());
            return super.remove(key);
        }

        private void deleteKey(final String key) throws RuntimeException {
            if (key == null) {
                throw new NullPointerException("key must not be null.");
            }

            Connection connection = null;
            try {
                connection = connectionProvider.getConnection();

                PreparedStatement ps = null;
                try {
                    if (connection.getMetaData().getDatabaseProductName().equals("MySQL")) { // Because MySQL doesn't like the key column name.
                        ps = connection.prepareStatement("DELETE FROM preferences WHERE preferences.userid = ? AND preferences.key = ?");
                    } else {
                        ps = connection.prepareStatement("DELETE FROM preferences WHERE userid = ? AND key = ?");
                    }
                    ps.setString(1, getUserKey());
                    ps.setString(2, key);

                } catch (SQLException se) {
                    logger.error("Problem creating a prepared statment.", se);
                    if (ps != null) ps.cancel();
                    final RuntimeException re = new RuntimeException("Problem creating prepared statement to delete value in preferences.");
                    re.initCause(se);
                    throw re;
                }

                final int count = ps.executeUpdate();
                if (count > 1) {
                    logger.debug("Deleted " + count + " rows for: " + getUserKey());
                }

            } catch (SQLException se) {
                logger.error("Problem getting a Connection from the connectionProvider", se);
                final RuntimeException re = new RuntimeException(se.getMessage());
                re.initCause(se);
                throw re;

            } finally {
                try {
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    logger.error("Problem closing the Connection", e);
                }
            }
        }

        private void insertKeyValue(final String key, final String value) throws RuntimeException {
            if (key == null) {
                throw new NullPointerException("key must not be null. value: " + value);
            } else if (value == null) {
                throw new NullPointerException("value must not be null. key: " + key);
            }
            Connection connection = null;
            try {
                connection = connectionProvider.getConnection();

                PreparedStatement ps = null;
                try {
                    if (connection.getMetaData().getDatabaseProductName().equals("MySQL")) { // Because MySQL doesn't like the key column name.
                        ps = connection.prepareStatement("INSERT INTO preferences " + "(preferences.userid, preferences.key, preferences.entry) " + "VALUES (?, ?, ?)");
                    } else {
                        ps = connection.prepareStatement("INSERT INTO preferences " + "(userid, key, entry) " + "VALUES (?, ?, ?)");
                    }
                    ps.setString(1, getUserKey());
                    ps.setString(2, key);
                    ps.setString(3, value);

                } catch (SQLException se) {
                    logger.error("Problem creating a prepared statment.", se);
                    if (ps != null) {
                        ps.cancel();
                        ps.close();
                    }
                    final RuntimeException re = new RuntimeException("Problem creating prepared statement to insert value in preferences.");
                    re.initCause(se);
                    throw re;
                }

                final int count = ps.executeUpdate();
                if (count == 0) {
                    throw new RuntimeException("Preferences insert failed for " + ps);
                }

                ps.close();

            } catch (SQLException se) {
                logger.error("Problem getting a Connection from the connectionProvider", se);
                final RuntimeException re = new RuntimeException(se.getMessage());
                re.initCause(se);
                throw re;

            } finally {
                try {
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    logger.error("Problem closing the Connection", e);
                }
            }
        }

        public String getUserKey() {
            return userKey;
        }

        private User getUser() {
            final User user;
            final Reference ref = (Reference)USERS.get();
            if (ref == null) {
                throw new NullPointerException("ref from USERS is null! This should never happen.");
            }
            user = (User)ref.get();
            if (user == null) {
                throw new NullPointerException("user from USERS is null! This should never happen.");
            }
            return user;
        }

        private HttpSession getHttpSession() {
            final HttpSession session;
            final Reference ref = (Reference)HTTP_SESSIONS.get();
            session = (HttpSession)ref.get();
            return session;
        }
    }

    public interface ConnectionProvider {
        public Connection getConnection() throws SQLException;
    }
}
