/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002-2004 William A. McArthur, Jr.
 * Copyright (C) 2003-2004 The Open Systems Group / University of Florida
 * Copyright (C) 2004 Allison Moore
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

package edu.ufl.osg.webmail.util;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Argument;
import edu.ufl.osg.webmail.Constants;
import edu.ufl.osg.webmail.User;
import edu.ufl.osg.webmail.data.AddressBkDAO;
import edu.ufl.osg.webmail.data.AddressBkDAOException;
import edu.ufl.osg.webmail.data.AddressList;
import edu.ufl.osg.webmail.data.AttachDAOException;
import edu.ufl.osg.webmail.data.AttachList;
import edu.ufl.osg.webmail.data.DAOFactory;
import org.apache.log4j.Logger;
import org.apache.struts.tiles.ComponentDefinition;
import org.apache.struts.tiles.DefinitionsFactory;
import org.apache.struts.tiles.DefinitionsFactoryException;
import org.apache.struts.tiles.TilesUtilImpl;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Iterator;

/**
 * Utility class with generic convience methods for use in various places.
 *
 * @author sandymac
 * @author drakee
 * @version $Revision: 1.8 $
 */
public class Util {
    private static final Logger logger = Logger.getLogger(Util.class.getName());

    private static final char QUOTE = '"';

    private static ResourceBundle bundle;

    private static final Date webAppInit = new Date();

    private static final List activeSessions = new LinkedList();

    public static void addMessageParams(final Message message, final String sort, final Map messageParams) throws MessagingException {
        messageParams.put("folder", message.getFolder().getFullName());

        final Folder folder = message.getFolder();
        messageParams.put("sort", sort);

        if (folder instanceof IMAPFolder) {
            final IMAPFolder imapFolder = (IMAPFolder)folder;
            messageParams.put("uid", new Long(imapFolder.getUID(message)));
        } else {
            messageParams.put("messageNumber", new Integer(message.getMessageNumber()));
        }
    }

    public static void addMessageParams(final Message message, final Map messageParams) throws MessagingException {
        messageParams.put("folder", message.getFolder().getFullName());

        final Folder folder = message.getFolder();

        if (folder instanceof IMAPFolder) {
            final IMAPFolder imapFolder = (IMAPFolder)folder;
            messageParams.put("uid", new Long(imapFolder.getUID(message)));
        } else {
            messageParams.put("messageNumber", new Integer(message.getMessageNumber()));
        }
    }

    /**
     * Always returns a JavaMail {@link javax.mail.Folder} prefering a cached on
     * if possible. This differers from {@link Util#getFolder(HttpSession,
     * String)} in that the returned {@link javax.mail.Folder} instance may be
     * the same one passed in as <code>folder</code>.
     *
     * @param folder The {@link javax.mail.Folder} to add to the
     *               user's session.
     * @return A open {@link javax.mail.Folder}.
     */
    public static Folder getFolder(final Folder folder) {
        if (!folder.isOpen()) {
            try {
                folder.open(Folder.READ_WRITE);
            } catch (MessagingException me) {
                // XXX Ignore errors for now
            }
        }
        FolderCloserFilter.closeFolder(folder);
        return folder;
    }

    public static final boolean traceProtocol = false;
    private static void addProtocolMarkers(final Folder folder, final String tag) {
        if (folder instanceof IMAPFolder) {
            IMAPFolder imapFolder = (IMAPFolder)folder;
            try {
                imapFolder.doCommandIgnoreFailure(new IMAPFolder.ProtocolCommand() {
                    public Object doCommand(IMAPProtocol imapProtocol) throws ProtocolException {
                        Argument argument = new Argument();
                        argument.writeString(tag);
                        imapProtocol.simpleCommand("XGATORMAIL-MARKER", argument);
                        return null;
                    }
                });
            } catch (MessagingException e) {
                //e.printStackTrace();
            }

        }
    }

    private static void addProtocolMarkers(final Folder folder, final String tag1, final String tag2) {
        if (folder instanceof IMAPFolder) {
            IMAPFolder imapFolder = (IMAPFolder)folder;
            try {
                imapFolder.doCommandIgnoreFailure(new IMAPFolder.ProtocolCommand() {
                    public Object doCommand(IMAPProtocol imapProtocol) throws ProtocolException {
                        Argument argument = new Argument();
                        argument.writeString(tag1);
                        argument.writeString(tag2);
                        imapProtocol.simpleCommand("XGATORMAIL-MARKER", argument);
                        return null;
                    }
                });
            } catch (MessagingException e) {
                //e.printStackTrace();
            }

        }
    }

    public static void addProtocolMarkers(final Folder folder, final StackTraceElement[] elements) {
        if (folder instanceof IMAPFolder) {
            IMAPFolder imapFolder = (IMAPFolder)folder;
            try {
                imapFolder.doCommandIgnoreFailure(new IMAPFolder.ProtocolCommand() {
                    public Object doCommand(IMAPProtocol imapProtocol) throws ProtocolException {
                        Argument argument = new Argument();
                        argument.writeString(folder.toString() + " (" + System.identityHashCode(folder) + ")");
                        int i;
                        for (i=0; i < elements.length && elements[i].getClassName().startsWith("edu.ufl.osg.webmail"); i++) {
                            argument.writeString(elements[i].toString());
                        }
                        argument.writeString(elements[i].toString());
                        argument.writeString(elements[++i].toString());
                        imapProtocol.simpleCommand("X-GATORMAIL-STACK", argument);
                        return null;
                    }
                });
            } catch (MessagingException e) {
                //e.printStackTrace();
            }

        }
    }

    /**
     * Always returns a JavaMail {@link javax.mail.Folder} or an
     * MessagingException is thrown. This will first try to get the folder from
     * the servlet session if one is still cached. If a cached folder isn't
     * available then a new one is fetched from the Store.
     *
     * @param session    The servlet session to look for cached
     *                   folders.
     * @param folderName The full name of the requested folder, eg:
     *                   INBOX.Trash
     * @return The Folder object for folderName.
     * @throws MessagingException Thrown if there is a problem accessing the
     *                            requested folder.
     */
    public static Folder getFolder(final HttpSession session, final String folderName) throws MessagingException {
        Folder folder = null;
        final Store store = getMailStore(session);
        if (!store.isConnected()) {
            store.connect();
        }
        folder = store.getFolder(folderName);
        FolderCloserFilter.closeFolder(folder);
        if (Util.traceProtocol) {
            Util.addProtocolMarkers(folder, (new Exception()).getStackTrace());
        }
        if (!folder.isOpen()) {
            folder.open(Folder.READ_WRITE);
        }
        FolderCloserFilter.closeFolder(folder);
        return folder;
    }

    public static void releaseFolder(final Folder folder) throws MessagingException {
        if (Util.traceProtocol) {
            Util.addProtocolMarkers(folder, (new Exception()).getStackTrace());
        }
        if (folder != null && folder.isOpen()) {
            folder.close(false);
        }
    }

    public static String getPartPath(final BodyPart bodyPart) throws MessagingException {
        final Multipart multipart = bodyPart.getParent();
        int partNumber;

        for (partNumber = 0; partNumber < multipart.getCount(); partNumber++) {
            if (multipart.getBodyPart(partNumber).equals(bodyPart)) {
                break;
            }
        }

        final Part parentPart = multipart.getParent();

        return ((parentPart instanceof BodyPart) ? (getPartPath((BodyPart)parentPart) + ".") : "") + partNumber;
    }

    public static boolean hasSubscribedSubfolder(final Folder folder) throws MessagingException {
        final Folder[] subscribedFolders = folder.listSubscribed("*");
        for (int i=0; i < subscribedFolders.length; i++) {
            if (subscribedFolders[i].isSubscribed()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Used to sort messages newest first, this will me moving out of Util
     * later.
     */
    public static class DateSort implements Comparator {
        /**
         * Compares its two arguments for order. Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.
         *
         * <p>The implementor must ensure that <tt>sgn(compare(x, y)) ==
         * -sgn(compare(y, x))</tt> for all <tt>x</tt> and <tt>y</tt>. (This
         * implies that <tt>compare(x, y)</tt> must throw an exception if and
         * only if <tt>compare(y, x)</tt> throws an exception.) </p>
         *
         * <p>The implementor must also ensure that the relation is transitive:
         * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt>
         * implies <tt>compare(x, z)&gt;0</tt>. </p>
         *
         * <p>Finally, the implementer must ensure that <tt>compare(x,
         * y)==0</tt> implies that <tt>sgn(compare(x, z))==sgn(compare(y,
         * z))</tt> for all <tt>z</tt>. </p>
         *
         * <p>It is generally the case, but <i>not</i> strictly required that
         * <tt>(compare(x, y)==0) == (x.equals(y))</tt>. Generally speaking, any
         * comparator that violates this condition should clearly indicate this
         * fact. The recommended language is "Note: this comparator imposes
         * orderings that are inconsistent with equals." </p>
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive
         *         integer as the first argument is less
         *         than, equal to, or greater than the
         *         second.
         */
        public int compare(final Object o1, final Object o2) {
            Date d1 = null;
            Date d2 = null;
            final Message m1 = (Message)o1;
            final Message m2 = (Message)o2;

            try {
                d1 = m1.getReceivedDate();
            } catch (MessagingException me) {
                me.printStackTrace();
            }

            try {
                d2 = m2.getReceivedDate();
            } catch (MessagingException me) {
                me.printStackTrace();
            }

            return d2.compareTo(d1);
        }
    }

    public static class DateSortU implements Comparator {
        /**
         * Compares its two arguments for order. Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive
         *         integer as the first argument is greater
         *         than, equal to, or less than the
         *         second.
         */
        public int compare(final Object o1, final Object o2) {
            Date d1 = null;
            Date d2 = null;

            final Message m1 = (Message)o1;
            final Message m2 = (Message)o2;

            try {
                d1 = m1.getReceivedDate();
            } catch (MessagingException me) {
                me.printStackTrace();
            }

            try {
                d2 = m2.getReceivedDate();
            } catch (MessagingException me) {
                me.printStackTrace();
            }

            return d1.compareTo(d2);
        }
    }

    public static class SubjectSort implements Comparator {
        /**
         * Compares its two arguments for order. Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive
         *         integer as the first argument is less
         *         than, equal to, or greater than the
         *         second.
         */
        public int compare(final Object o1, final Object o2) {
            String d1 = null;
            String d2 = null;

            final Message m1 = (Message)o1;

            final Message m2 = (Message)o2;

            try {
                d1 = m1.getSubject();
                d2 = m2.getSubject();

                if (d1 != null) {
                    d1 = d1.trim();

                    boolean done = false;

                    while (!done) {

                        final String threadSubjectLC = d1.toLowerCase();

                        if (threadSubjectLC.startsWith("re:") || threadSubjectLC.startsWith("fw:")) {
                            d1 = d1.substring(3).trim();

                        } else if (threadSubjectLC.startsWith("fwd:")) {
                            d1 = d1.substring(4).trim();

                        } else if (threadSubjectLC.startsWith("[fwd:") && d1.endsWith("]")) {
                            d1 = d1.substring(5, d1.length() - 1).trim();

                        } else if (threadSubjectLC.endsWith("(fwd)")) {
                            d1 = d1.substring(0, d1.length() - 5).trim();

                        } else {
                            done = true;

                        }
                    }
                } else {
                    d1 = "";
                }

                if (d2 != null) {
                    d2 = d2.trim();

                    boolean done = false;

                    while (!done) {

                        final String threadSubjectLC = d2.toLowerCase();

                        if (threadSubjectLC.startsWith("re:") || threadSubjectLC.startsWith("fw:")) {
                            d2 = d2.substring(3).trim();

                        } else if (threadSubjectLC.startsWith("fwd:")) {
                            d2 = d2.substring(4).trim();

                        } else if (threadSubjectLC.startsWith("[fwd:") && d2.endsWith("]")) {
                            d2 = d2.substring(5, d2.length() - 1).trim();

                        } else if (threadSubjectLC.endsWith("(fwd)")) {
                            d2 = d2.substring(0, d2.length() - 5).trim();

                        } else {
                            done = true;

                        }

                    }

                } else {
                    d2 = "";
                }

                d1 = d1.toLowerCase();
                d2 = d2.toLowerCase();

            } catch (MessagingException me) {
                me.printStackTrace();
            }

            return d1.compareTo(d2);
        }
    }

    public static class SubjectSortU implements Comparator {
        /**
         * Compares its two arguments for order. Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive
         *         integer as the first argument is greater
         *         than, equal to, or less than the
         *         second.
         */

        public int compare(final Object o1, final Object o2) {
            String d1 = null;
            String d2 = null;

            final Message m1 = (Message)o1;
            final Message m2 = (Message)o2;

            try {
                d1 = m1.getSubject();
                d2 = m2.getSubject();

                if (d1 != null) {
                    d1 = d1.trim();

                    boolean done = false;

                    while (!done) {

                        final String threadSubjectLC = d1.toLowerCase();

                        if (threadSubjectLC.startsWith("re:") || threadSubjectLC.startsWith("fw:")) {
                            d1 = d1.substring(3).trim();

                        } else if (threadSubjectLC.startsWith("fwd:")) {
                            d1 = d1.substring(4).trim();

                        } else if (threadSubjectLC.startsWith("[fwd:") && d1.endsWith("]")) {
                            d1 = d1.substring(5, d1.length() - 1).trim();

                        } else if (threadSubjectLC.endsWith("(fwd)")) {
                            d1 = d1.substring(0, d1.length() - 5).trim();

                        } else {
                            done = true;

                        }
                    }
                } else {
                    d1 = "";
                }

                if (d2 != null) {
                    d2 = d2.trim();

                    boolean done = false;

                    while (!done) {

                        final String threadSubjectLC = d2.toLowerCase();

                        if (threadSubjectLC.startsWith("re:") || threadSubjectLC.startsWith("fw:")) {
                            d2 = d2.substring(3).trim();

                        } else if (threadSubjectLC.startsWith("fwd:")) {
                            d2 = d2.substring(4).trim();

                        } else if (threadSubjectLC.startsWith("[fwd:") && d2.endsWith("]")) {
                            d2 = d2.substring(5, d2.length() - 1).trim();

                        } else if (threadSubjectLC.endsWith("(fwd)")) {
                            d2 = d2.substring(0, d2.length() - 5).trim();

                        } else {
                            done = true;

                        }

                    }

                } else {
                    d2 = "";
                }

                d1 = d1.toLowerCase();
                d2 = d2.toLowerCase();

            } catch (MessagingException me) {
                me.printStackTrace();

            }

            return d2.compareTo(d1);

        }

    }

    public static class FromSort implements Comparator {
        /**
         * Compares its two arguments for order. Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive
         *         integer as the first argument is less
         *         than, equal to, or greater than the
         *         second.
         */
        public int compare(final Object o1, final Object o2) {
            Address[] d1 = null;
            Address[] d2 = null;

            final Message m1 = (Message)o1;

            final Message m2 = (Message)o2;

            int result = 0;

            try {
                d2 = m2.getFrom();
            } catch (MessagingException me) {
                me.printStackTrace();
            }

            try {
                d1 = m1.getFrom();
            } catch (MessagingException me) {
                me.printStackTrace();
            }

            if (d1 == null && d2 == null) return 0;

            if (d1 == null && d2 != null) return -1;

            if (d1 != null && d2 == null) return 1;

            if (d1.length == 0 && d2.length == 0) return 0;

            if (d1.length == 0 && d2.length != 0) return -1;

            if (d1.length != 0 && d2.length == 0) return 1;

            int i = 0;

            while (d1.length > i && d2.length > i) {
                final Object a = d1[i];
                final Object b = d2[i];

                if (a == null && b == null) return 0;
                final InternetAddress internetAddressA = (InternetAddress)a;
                final InternetAddress internetAddressB = (InternetAddress)b;
                String aa = internetAddressA.getPersonal();
                String bb = internetAddressB.getPersonal();

                if (aa == null) {
                    aa = internetAddressA.getAddress();
                }

                if (bb == null) {
                    bb = internetAddressB.getAddress();
                }

                aa = aa.toLowerCase().replace(QUOTE, ' ').trim();
                bb = bb.toLowerCase().replace(QUOTE, ' ').trim();

                if (aa.compareTo(bb) == 0) {
                    i++;

                } else {
                    result = aa.compareTo(bb);
                    i = d1.length;
                }
            }

            return result;
        }
    }

    public static class FromSortU implements Comparator {

        /**
         * Compares its two arguments for order. Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive
         *         integer as the first argument is greater
         *         than, equal to, or less than the
         *         second.
         */
        public int compare(final Object o1, final Object o2) {
            Address[] d1 = null;
            Address[] d2 = null;

            final Message m1 = (Message)o1;

            final Message m2 = (Message)o2;

            int result = 0;

            try {
                d1 = m1.getFrom();
            } catch (MessagingException me) {
                me.printStackTrace();
            }

            try {
                d2 = m2.getFrom();
            } catch (MessagingException me) {
                me.printStackTrace();
            }

            if (d1 == null && d2 == null) return 0;

            if (d1 == null && d2 != null) return 1;

            if (d1 != null && d2 == null) return -1;

            if (d1.length == 0 && d2.length == 0) return 0;

            if (d1.length == 0 && d2.length != 0) return 1;

            if (d1.length != 0 && d2.length == 0) return -1;

            int i = 0;

            while (d1.length > i && d2.length > i) {
                final Object a = d1[i];
                final Object b = d2[i];

                if (a == null && b == null) return 0;
                final InternetAddress internetAddressA = (InternetAddress)a;
                final InternetAddress internetAddressB = (InternetAddress)b;
                String aa = internetAddressA.getPersonal();
                String bb = internetAddressB.getPersonal();

                if (aa == null) {
                    aa = internetAddressA.getAddress();
                }

                if (bb == null) {
                    bb = internetAddressB.getAddress();
                }

                aa = aa.toLowerCase().replace(QUOTE, ' ').trim();
                bb = bb.toLowerCase().replace(QUOTE, ' ').trim();

                if (bb.compareTo(aa) == 0) {
                    i++;

                } else {
                    result = bb.compareTo(aa);
                    i = d1.length;
                }
            }

            return result;
        }

    }

    public static class ToSort implements Comparator {
        /**
         * Compares its two arguments for order. Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive
         *         integer as the first argument is less
         *         than, equal to, or greater than the
         *         second.
         */
        public int compare(final Object o1, final Object o2) {
            Address[] d1 = null;
            Address[] d2 = null;

            final Message m1 = (Message)o1;
            final Message m2 = (Message)o2;

            int result = 0;

            try {
                d1 = m1.getRecipients(Message.RecipientType.TO);
            } catch (MessagingException me) {
                me.printStackTrace();
            }

            try {
                d2 = m2.getRecipients(Message.RecipientType.TO);
            } catch (MessagingException me) {
                me.printStackTrace();
            }

            if (d1 == null && d2 == null) return 0;

            if (d1 == null && d2 != null) return -1;

            if (d1 != null && d2 == null) return 1;

            if (d1.length == 0 && d2.length == 0) return 0;

            if (d1.length == 0 && d2.length != 0) return -1;

            if (d1.length != 0 && d2.length == 0) return 1;

            int i = 0;

            while (d1.length > i && d2.length > i) {
                final Object a = d1[i];
                final Object b = d2[i];

                if (a == null && b == null) return 0;
                final InternetAddress internetAddressA = (InternetAddress)a;
                final InternetAddress internetAddressB = (InternetAddress)b;
                String aa = internetAddressA.getPersonal();
                String bb = internetAddressB.getPersonal();

                if (aa == null) {
                    aa = internetAddressA.getAddress();
                }

                if (bb == null) {
                    bb = internetAddressB.getAddress();
                }

                aa = aa.toLowerCase().replace(QUOTE, ' ').trim();
                bb = bb.toLowerCase().replace(QUOTE, ' ').trim();

                if (aa.compareTo(bb) == 0) {
                    i++;

                } else {
                    result = aa.compareTo(bb);
                    i = d1.length;
                }
            }

            return result;
        }
    }

    public static class ToSortU implements Comparator {
        /**
         * Compares its two arguments for order. Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive
         *         integer as the first argument is greater
         *         than, equal to, or less than the
         *         second.
         */
        public int compare(final Object o1, final Object o2) {
            Address[] d1 = null;
            Address[] d2 = null;

            final Message m1 = (Message)o1;
            final Message m2 = (Message)o2;

            int result = 0;

            try {
                d1 = m1.getRecipients(Message.RecipientType.TO);
            } catch (MessagingException me) {
                me.printStackTrace();
            }

            try {
                d2 = m2.getRecipients(Message.RecipientType.TO);
            } catch (MessagingException me) {
                me.printStackTrace();
            }

            if (d1 == null && d2 == null) return 0;

            if (d1 == null && d2 != null) return 1;

            if (d1 != null && d2 == null) return -1;

            if (d1.length == 0 && d2.length == 0) return 0;

            if (d1.length == 0 && d2.length != 0) return 1;

            if (d1.length != 0 && d2.length == 0) return -1;

            int i = 0;

            while (d1.length > i && d2.length > i) {
                final Object a = d1[i];
                final Object b = d2[i];

                if (a == null && b == null) return 0;
                final InternetAddress internetAddressA = (InternetAddress)a;
                final InternetAddress internetAddressB = (InternetAddress)b;
                String aa = internetAddressA.getPersonal();
                String bb = internetAddressB.getPersonal();

                if (aa == null) {
                    aa = internetAddressA.getAddress();
                }

                if (bb == null) {
                    bb = internetAddressB.getAddress();
                }

                aa = aa.toLowerCase().replace(QUOTE, ' ').trim();
                bb = bb.toLowerCase().replace(QUOTE, ' ').trim();

                if (bb.compareTo(aa) == 0) {
                    i++;

                } else {
                    result = bb.compareTo(aa);
                    i = d1.length;

                }
            }

            return result;
        }
    }

    public static class SizeSort implements Comparator {
        /**
         * Compares its two arguments for order. Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive
         *         integer as the first argument is less
         *         than, equal to, or greater than the
         *         second.
         */
        public int compare(final Object o1, final Object o2) {

            int d1 = 0;
            int d2 = 0;

            final Message m1 = (Message)o1;
            final Message m2 = (Message)o2;

            try {
                d1 = m1.getSize();
            } catch (MessagingException me) {
                me.printStackTrace();
            }

            try {
                d2 = m2.getSize();
            } catch (MessagingException me) {
                me.printStackTrace();
            }

            if (d1 < d2) return -1;
            else if (d1 == d2) return 0;
            else return 1;
        }
    }

    public static class SizeSortU implements Comparator {
        /**
         * Compares its two arguments for order. Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive
         *         integer as the first argument is greater
         *         than, equal to, or less than the
         *         second.
         */
        public int compare(final Object o1, final Object o2) {

            int d1 = 0;
            int d2 = 0;

            final Message m1 = (Message)o1;
            final Message m2 = (Message)o2;

            try {
                d1 = m1.getSize();
            } catch (MessagingException me) {
                me.printStackTrace();
            }

            try {
                d2 = m2.getSize();
            } catch (MessagingException me) {
                me.printStackTrace();
            }

            if (d2 < d1) return -1;
            else if (d1 == d2) return 0;
            else return 1;
        }
    }

    /**
     * Get messages from "Messages.properties" ResourceBundle
     */
    public static String getFromBundle(final String key) {
        if (bundle == null) bundle = ResourceBundle.getBundle("Messages");
        return bundle.getString(key);
    }

    /**
     * Get User object from the HttpSession.
     */
    public static User getUser(final HttpSession session) {
        return (User)session.getAttribute(Constants.USER_KEY);
    }

    /**
     * Retrieve the user's address book. If it isn't in the current
     * session, then fetch it anew from the data source.
     */
    public static AddressList getAddressList(final HttpSession session) throws AddressBkDAOException {
        AddressList addressList = (AddressList)session.getAttribute(Constants.ADDRESS_LIST);
        if (addressList == null) {
            // no address book in session - look it up
            final User user = getUser(session);
            final AddressBkDAO addressBkDAO = DAOFactory.getInstance().getAddressBkDAO();
            addressList = addressBkDAO.getAddressList(user.getPermId());
            session.setAttribute(Constants.ADDRESS_LIST, addressList);
        }
        return addressList;
    }

    /**
     * Retrieve current attachments from session. If the attachment list does not
     * exist, then return a blank AttachList. Never return null.
     */
    public static AttachList getAttachList(final String composeKey, final HttpSession session) throws AttachDAOException {
        Map attachMap = getAttachMap(session);
        AttachList attachList = (AttachList)attachMap.get(composeKey);
        // new compose session, let's add it to the attachMap
        if (attachList == null) {
            // It is possible to create AttachLists that get stored in attachMap
            // and never get removed from the session. We want to prevent people
            // from maliciously trying to hog memory by creating huge numbers of
            // attachList entries.
            if (attachMap.size() > 2000) { // arbitrarily huge number
                final User user = getUser(session);
                logger.warn("User " + user + " has " + attachMap.size() + " attachList entries!" + " Resetting attachMap.");
                // give the user a new, empty attachMap. All his previous compose
                // sessions will be lost, though the new one will be fine.
                session.removeAttribute(Constants.ATTACHMENT_MAP);
                attachMap = getAttachMap(session);
            }
            attachList = new AttachList();
            attachMap.put(composeKey, attachList);
        }
        logger.debug("in getAttachList(). attachMap now has: " + attachMap.size() + " items");
        return attachList;
    }

    /**
     * Retrieve current attachments from session. If the attachment list does not
     * exist, then return a blank AttachList. Never return null.
     */
    public static void setAttachList(final AttachList attachList, final String composeKey, final HttpSession session) {
        final Map attachMap = getAttachMap(session);
        attachMap.put(composeKey, attachList);
    }

    /**
     * Removes an attachment list from the session.
     */
    public static void removeAttachList(final String composeKey, final HttpSession session) {
        final Map attachMap = getAttachMap(session);
        attachMap.remove(composeKey);
        logger.debug("removed key=" + composeKey + " from attachMap.");
        logger.debug("attachMap now contains: " + attachMap.keySet());
    }

    /**
     * Return a unique key for the Attachment Map session object.
     */
    public static String generateComposeKey(final HttpSession session) {
        final Map attachMap = getAttachMap(session);
        int i = 0;
        while (attachMap.containsKey(String.valueOf(i))) {
            i++;
        }
        return String.valueOf(i);
    }

    /**
     * Retrieve map of AttachLists from session. If the Map doesn't
     * exist, then return a new Map. Never return null.
     */
    private static Map getAttachMap(final HttpSession session) {
        Map attachMap = (Map)session.getAttribute(Constants.ATTACHMENT_MAP);
        if (attachMap == null) {
            attachMap = new HashMap();
            session.setAttribute(Constants.ATTACHMENT_MAP, attachMap);
        }
        return attachMap;
    }

    /**
     * Converts a mime type to a tiles definition name. Normalizes a mime type
     * by lower casing it and stripping off any extra info. eg: "text/PLAIN;
     * charset=us-ascii; format=flowed" becomes "message.text/plain"
     *
     * @param mimeType The mime type.
     * @return a definition name.
     */
    public static String getDefinitionName(String mimeType) {
        mimeType = mimeType.toLowerCase();
        if (mimeType.indexOf(";") >= 0) {
            mimeType = mimeType.substring(0, mimeType.indexOf(";"));
        }

        // I don't think this will ever be true but just in case.
        if (mimeType.indexOf(" ") >= 0) {
            mimeType = mimeType.substring(0, mimeType.indexOf(" "));
        }

        // TODO: the "message." shouldn't be hard coded.
        return "message." + mimeType;
    }

    /**
     * Returns true if we have a tiles definition for a mime type.
     *
     * @param mimeType The mime type.
     * @param request  The http request.
     * @param context  the servlet context.
     * @return true only if we have a tile to deal with this
     *         mime type.
     */
    public static boolean isValidDefinition(final String mimeType, final ServletRequest request, final ServletContext context) {
        final DefinitionsFactory df = (DefinitionsFactory)context.getAttribute(TilesUtilImpl.DEFINITIONS_FACTORY);
        //mimeType = getDefinitionName(mimeType);
        ComponentDefinition definition = null;
        try {
            definition = df.getDefinition(mimeType, request, context);
        } catch (DefinitionsFactoryException e) {
            e.printStackTrace();
        }
        return definition != null;
    }

    /**
     * Is this a special, undeletable folder such as INBOX or Trash?
     */
    public static boolean isReservedFolder(final String folderName, final HttpSession session) throws MessagingException {
        return ("INBOX".equals(folderName) || Constants.getTrashFolderFullname(session).equals(folderName) || Constants.getSentFolderFullname(session).equals(folderName));
    }

    // Two methods to help track usage stats. TODO: remove them
    public static List getActiveSessions() {
        final List list;
        synchronized (activeSessions) {
            list = new ArrayList(activeSessions.size());
            final Iterator iter = activeSessions.iterator();
            while (iter.hasNext()) {
                final Reference ref = (Reference)iter.next();
                if (ref.get() != null) {
                    list.add(ref.get());
                } else {
                    iter.remove();
                }
            }
        }
        return list;
    }

    public static void addActiveSession(final User user) {
        synchronized (activeSessions) {
            activeSessions.add(new WeakReference(user));
        }
    }

    public static void removeActiveSession(final User user) {
        synchronized (activeSessions) {
            final Iterator iter = activeSessions.iterator();
            while (iter.hasNext()) {
                final Reference ref = (Reference)iter.next();
                if (ref.get() != null) {
                    final User u = (User)ref.get();
                    if (user.equals(u)) {
                        iter.remove();
                    }
                } else {
                    iter.remove();
                }
            }
        }
    }

    /**
     * Builds a String holding Exception information, useful for logging.
     *
     * @return a formatted error string, containing the User's permId and displayName
     *         (if user isn't null), as well as the stack trace of the error (truncated if too long).
     */
    public static String buildErrorMessage(final Exception ex, final User user) {
        final StringBuffer sb = new StringBuffer();
        if (user != null) {
            sb.append("ERROR! (user: " + user.getUsername() + ", name: " + user.getDisplayName() + ", id: " + user.getPermId() + ")\n");
        }
        // capture stack trace
        final StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        final StringBuffer errSb = sw.getBuffer();
        // truncate if stack trace is overly long
        //sb.append((errSb.length() > 2048) ? errSb.substring(0, 2048) : errSb.toString());
        sb.append(errSb.toString());
        return sb.toString();
    }

    public static boolean hasAttachment(final Message message) throws MessagingException {
        final boolean hasAttachment = false;

        final Flags flags;

        flags = message.getFlags();
        if (flags.contains(Constants.MESSAGE_FLAG_HAS_ATTACHMENT)) {
            return true;
        } else if (flags.contains(Constants.MESSAGE_FLAG_NO_ATTACHMENT)) {
            return false;
        }

        // No pre-existing flags

        // We don't traverse the message beause this is called from the view and that would alter the SEEN state.
        //if (message.isMimeType("text/*") || message.isMimeType("multipart/*")) {
        //    try {
        //        MimeMultipart mmp = (MimeMultipart)message.getContent();
        //        hasAttachment = hasAttachment(mmp);
        //    } catch (Exception e) {
        //         Do nothing
        //    }
        //}

        return hasAttachment;
    }

    public static boolean hasAttachment(final MimeMultipart mmp) throws MessagingException, IOException {
        for (int i = 0; i < mmp.getCount(); i++) {
            final BodyPart part = mmp.getBodyPart(i);
            // This is too agressive in attachment detection
            //if (part.getDisposition() == null || part.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {
            //    return true;
            //}
            if (!part.isMimeType("text/*") && !part.isMimeType("multipart/*")) {
                return true;
            }

            boolean ha = false;
            if (part.isMimeType("multipart/*")) {
                ha = hasAttachment((MimeMultipart)part.getContent());
            }
            if (ha) {
                return ha;
            }
        }
        return false;
    }

    public static Session getMailSession(final HttpSession httpSession) {
        final User user = getUser(httpSession);
        return user.getMailSession();
    }

    public static Store getMailStore(final HttpSession httpSession) throws NoSuchProviderException {
        final User user = getUser(httpSession);
        return user.getMailStore();
    }

    /**
     * The time this web application was started.
     *
     * @return The time this web application was started.
     */
    public static Date getWebappinit() {
        return webAppInit != null ? (Date)webAppInit.clone() : null;
    }
}