
package edu.ufl.osg.webmail.imap;

import edu.ufl.osg.webmail.SessionProvider;
import edu.ufl.osg.webmail.User;
import edu.ufl.osg.webmail.util.Util;
import edu.ufl.osg.webmail.util.FolderCloserFilter;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.servlet.ServletRequest;
import java.util.Properties;

/**
 * A Session provider for UF's single sign on efforts.
 *
 * @author sandymac
 * @version $Revision: 1.3 $
 * @since Aug 4, 2004
 */
public class UFIMAPSessionProvider implements SessionProvider {
    private final String protocol;
    private final Properties props;
    private final String authentication;
    private final String password;

    private final String SASL_AUTH_KEY;

    public UFIMAPSessionProvider(String protocol, Properties props, String authentication, String password) {
        this.protocol = protocol;
        this.props = props;
        this.authentication = authentication;
        this.password = password;

        SASL_AUTH_KEY = "mail." + protocol + ".sasl.authorizationid";
    }

    /**
     * Returns an object with information about the user. The request parameter
     * is so that the {@link edu.ufl.osg.webmail.SessionProvider} plugin can access thing like cookies.
     *
     * @param username The user's username.
     * @param password The user's password.
     * @param request  The ServletRequest.
     * @return A user object.
     */
    public User getUser(String username, String password, ServletRequest request) {
        final String glid = (String)request.getAttribute("UF_GatorLinkID");
        return new UFUser(glid);
    }

    /**
     * Returns a JavaMail Authenticator to be used when the JavaMail session
     * connects.
     *
     * @param user User from which the username is read. Must be of type {@link UFUser}.
     * @return The Authenticator to be used to connect.
     */
    public Authenticator getAuthenticator(final User user) {
        return new UFAuthenticator((UFUser)user);
    }

    /**
     * Gets a JavaMail session to connect to the mail store.
     *
     * @param authenticator JavaMail Authenticator to be used during
     *                      connect.
     * @return A JavaMail session.
     */
    public Session getSession(final Authenticator authenticator) {
        final UFAuthenticator ufAuthenticator = (UFAuthenticator)authenticator;
        final Properties props = new Properties(this.props);
        props.setProperty(SASL_AUTH_KEY, ufAuthenticator.getAuthorizationId());
        final Session session = Session.getInstance(props, authenticator);
        //session.setDebug(true);

        Store store = null;
        try {
            store = session.getStore();

            store.connect();
            Folder folder = store.getFolder("INBOX");
            FolderCloserFilter.closeFolder(folder);
            if (Util.traceProtocol) {
                Util.addProtocolMarkers(folder, (new Exception()).getStackTrace());
            }
            try {
                folder.open(Folder.READ_ONLY);
            } catch (MessagingException e) {
                e.printStackTrace(); // frequent
            } finally {
                if (folder.isOpen()) {
                    try {
                        folder.close(false);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (MessagingException e) {
            e.printStackTrace();
        } finally {
            if (store != null && store.isConnected()) {
                try {
                    store.close();
                } catch (MessagingException e1) {
                    e1.printStackTrace();
                }
            }

        }
        return session;
    }

    private class UFAuthenticator extends Authenticator {
        final private UFUser user;

        public UFAuthenticator(final UFUser user) {
            if (user == null) throw new NullPointerException("user must not be null.");
            this.user = user;
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(authentication, password);
        }

        public String getAuthorizationId() {
            return user.getUsername();
        }
    }

    public static class UFUser extends User {
        private String glid;

        UFUser(final String glid) {
            super(glid, null);

            if (glid == null) {
                throw new NullPointerException("UFUser: glid cannot be null!");
            }

            this.glid = glid;
        }

        /**
         * Getter for property password.
         *
         * @return Value of property password.
         */
        public String getPassword() {
            return null;
        }

        /**
         * Getter for property username.
         *
         * @return Value of property username.
         */
        public String getUsername() {
            return glid;
        }

        /**
         * Getter for property email.
         *
         * @return Value of property email.
         */
        public String getEmail() {
            return glid.toLowerCase() + "@ufl.edu";
        }
    }
}
