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

package edu.ufl.osg.webmail.data;

import edu.ufl.osg.webmail.User;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.apache.log4j.Logger;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Implementation for obtaining and setting user information via LDAP.
 *
 * @author drakee
 * @author sandymac
 * @version $Revision: 1.7 $
 */
public class UserInfoDAO_LDAP implements UserInfoDAO {
    private static final Logger logger = Logger.getLogger(UserInfoDAO_LDAP.class.getName());

    private Hashtable env;
    private boolean piercePrivacy;
    private String userBinding;
    private String attr_permId;
    private String attr_displayName;
    private String[] permIdArray;
    private String[] displayNameArray;
    private String[] allInfoArray;
    private static final StackObjectPool ldapConnections = new StackObjectPool();

    /** Only the DAOFactory instantiates this class */
    protected UserInfoDAO_LDAP() throws UserInfoDAOException {
        ConfigDAO configDAO = null;
        try {
            configDAO = DAOFactory.getInstance().getConfigDAO();
        } catch (Exception e) {
            throw new UserInfoDAOException(e.toString());
        }
        attr_permId = configDAO.getProperty("ldapAttrPermId");
        attr_displayName = configDAO.getProperty("ldapAttrDisplayName");
        permIdArray = new String[]{attr_permId};
        displayNameArray = new String[]{attr_displayName};
        allInfoArray = new String[]{attr_permId, attr_displayName};

        // are we going to pierce privacy, or bind as the user?
        piercePrivacy = Boolean.valueOf(configDAO.getProperty("ldapPiercePrivacy")).booleanValue();

        // populate hashtable with shared dirContext properties
        env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, configDAO.getProperty("ldapProviderUrl"));
        // set authenticated dir context
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        if (piercePrivacy) {
            env.put(Context.SECURITY_PRINCIPAL, configDAO.getProperty("ldapSecurityPrincipal"));
            String credentials = configDAO.getProperty("ldapSecurityCredentials");
            if ("true".equals(configDAO.getProperty("passwordsBase64Encoded")) && credentials != null) {
                try {
                    final InputStream in = MimeUtility.decode(new ByteArrayInputStream(credentials.getBytes("US-ASCII")), "base64");
                    final BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    // XXX This assumes there is no new line in the password.
                    credentials = br.readLine();
                } catch (MessagingException e) {
                    final UserInfoDAOException ue = new UserInfoDAOException("MessagingException: " + e.getMessage());
                    ue.initCause(e);
                    throw ue;

                } catch (UnsupportedEncodingException e) {
                    final UserInfoDAOException ue = new UserInfoDAOException("UnsupportedEncodingException: " + e.getMessage());
                    ue.initCause(e);
                    throw ue;

                } catch (IOException e) {
                    final UserInfoDAOException ue = new UserInfoDAOException("IOException: " + e.getMessage());
                    ue.initCause(e);
                    throw ue;
                }
            }
            env.put(Context.SECURITY_CREDENTIALS, credentials);
            if (piercePrivacy) {
                ldapConnections.setFactory(new LdapConnectionFactory(env));
            }
        } else {
            // get user binding (for not piercing privacy, e.g. binding as user)
            userBinding = configDAO.getProperty("ldapUserBinding");
        }
    }

    /**
     * get the user's display name, using a password.
     */
    public String getDisplayName(final String username) throws UserInfoDAOException {
        return getDisplayName(username, null);
    }

    /**
     * get the user's display name, using a password.
     */
    public String getDisplayName(final String username, final String password) throws UserInfoDAOException {
        String displayName = null;
        try {
            final Properties props = fetchInfo(displayNameArray, username, password);
            displayName = props.getProperty(attr_displayName);
        } catch (NamingException e) {
            throw new UserInfoDAOException(e.toString());
        }
        // should never be a null value
        if (displayName == null) {
            throw new UserInfoDAOException("Null displayName value for " + username);
        }
        return displayName;
    }

    /**
     * set the permanent ID (UF ID in this case).
     */
    public String getPermId(final String username) throws UserInfoDAOException {
        return getPermId(username, null);
    }

    /**
     * get the permanent ID (UF ID in this case).
     */
    public String getPermId(final String username, final String password) throws UserInfoDAOException {
        String permId = null;
        try {
            final Properties props = fetchInfo(permIdArray, username, password);
            permId = props.getProperty(attr_permId);
        } catch (NamingException e) {
            throw new UserInfoDAOException(e.toString());
        }
        // should never be a null value
        if (permId == null) {
            throw new UserInfoDAOException("Null permId value for " + username);
        }
        return permId;
    }

    /**
     * Initializes all the user info in a User object at once -
     * This is more efficient than doing separate LDAP lookups
     * as each field is needed.
     */
    public void setUserInfo(final User user) throws UserInfoDAOException {
        try {
            doSetUserInfo(user);
        } catch (Exception e) {
            try {
                logger.warn("First LDAP attempt failed for " + user.getDisplayName() + ", trying again (" + e + ")");
                Thread.sleep(1000);  // one second delay
                doSetUserInfo(user); // try again
            } catch (Exception e2) {
                // second try failed, now throw error
                final UserInfoDAOException userInfoDAOException = new UserInfoDAOException(e2.toString());
                userInfoDAOException.initCause(e);
                throw userInfoDAOException;
            }
        }
    }

    // Tries to fetch the user data out of LDAP and sets it in the User Object.
    private void doSetUserInfo(final User user) throws NamingException, UserInfoDAOException {
        final Properties props = fetchInfo(allInfoArray, user.getUsername(), user.getPassword());
        final String permId = props.getProperty(attr_permId);
        // should never be a null value
        if (permId == null) {
            throw new UserInfoDAOException("Null permId value for " + user.getUsername());
        }

        user.setPermId(permId);
        user.setDisplayName(props.getProperty(attr_displayName));
    }

    /**
     * Returns a Hashtable, populated with the user-appropriate DirContext values
     */
    private Hashtable getEnv(final String username, final String password) {
        final Hashtable userEnv = new Hashtable(env);
        // get authenticated dir context
        final String securityPrincipal = MessageFormat.format(userBinding, new String[]{username});
        userEnv.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
        userEnv.put(Context.SECURITY_CREDENTIALS, password);
        return userEnv;
    }

    /**
     * Returns a user's information. If piercePrivacy is false, the method will bind
     * to LDAP as this user. If piercePrivacy is true, it will bind as the configured
     * privacy-piercing user, and the password parameter is ignored.
     *
     * @param attrIDs attribute(s) to return
     * @param username the mail id of the user
     * @param password the LDAP password of the user. Ignored if piercePrivacy is true.
     * @return a Properties of the found values
     */
    private Properties fetchInfo(final String[] attrIDs, final String username, final String password) throws NamingException {
        // set initial context
        logger.debug("setting InitialDirContext...");
        final Properties props;
        DirContext ctx = null;
        try {
            if (piercePrivacy) {
                // TODO: Pool here
                ctx = (DirContext)ldapConnections.borrowObject();
                //ctx = new InitialDirContext(env);
            } else {
                ctx = new InitialDirContext(getEnv(username, password));
            }

            logger.info("fetchInfo for user: " + username);
            props = new Properties();

            // specify attributes to match
            final Attributes matchAttrs = new BasicAttributes(true);
            matchAttrs.put(new BasicAttribute("uid", username));

            final NamingEnumeration answer = ctx.search("ou=people", matchAttrs, attrIDs);

            // cycle through results
            while (answer.hasMore()) {
                final SearchResult sr = (SearchResult)answer.next();
                logger.debug("Search results for: " + sr.getName());
                // print out all attributes
                final Attributes attributes = sr.getAttributes();
                final NamingEnumeration ne = attributes.getAll();
                // cycle through attributes of this search result
                while (ne.hasMore()) {
                    final Attribute at = (Attribute)ne.next();
                    props.setProperty(at.getID(), at.get().toString());
                    logger.debug("attr. ID: " + at.getID() + ", value: " + at.get());
                }
                logger.info("LDAP Lookup for " + username + " containes: " + props);
                ne.close();
            }
            answer.close();
        } catch (Exception e) {
            try {
                ldapConnections.invalidateObject(ctx);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            throw new RuntimeException(e); // TODO: Remove me?
        } finally {
            if (piercePrivacy) {
                try {
                    ldapConnections.returnObject(ctx);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                if (ctx != null) {
                    ctx.close();
                }
            }
        }

        return props;
    }

    private class LdapConnectionFactory extends BasePoolableObjectFactory {
        private final Hashtable env;
        final Attributes matchAttrs;

        public LdapConnectionFactory(Hashtable env) {
            this.env = env;
            matchAttrs = new BasicAttributes(true);
            matchAttrs.put(new BasicAttribute("uid", "alberta"));
        }

        public Object makeObject() throws Exception {
            return new InitialDirContext(env);
        }

        public void destroyObject(Object obj) throws Exception {
            DirContext ctx = (DirContext)obj;
            ctx.close();

            StackObjectPool sop;
            GenericObjectPool gop;
        }

        public boolean validateObject(Object obj) {
            DirContext ctx = (DirContext)obj;
            // specify attributes to match
            NamingEnumeration answer = null;
            try {
                answer = ctx.search("ou=people", matchAttrs, permIdArray);
            } catch (NamingException e) {
                return false;
            } finally {
                if (answer != null) {
                    try {
                        answer.close();
                    } catch (NamingException e) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
