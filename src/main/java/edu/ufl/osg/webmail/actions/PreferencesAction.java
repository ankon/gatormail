/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2003,2006 The Open Systems Group / University of Florida
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

package edu.ufl.osg.webmail.actions;

import edu.ufl.osg.managesieve.Response;
import edu.ufl.osg.managesieve.Session;
import edu.ufl.osg.managesieve.Token;
import edu.ufl.osg.webmail.Constants;
import edu.ufl.osg.webmail.User;
import edu.ufl.osg.webmail.forms.PreferencesForm;
import edu.ufl.osg.webmail.prefs.PreferencesProvider;
import edu.ufl.osg.webmail.util.Util;
import edu.ufl.osg.webmail.util.FolderCloserFilter;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.mail.Folder;
import javax.mail.Store;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedReader;
import java.io.StringReader;

/**
 * Loads and saves the peferences.
 *
 * @author sandymac
 * @since  Aug 28, 2003 1:28:15 PM
 * @version $Revision: 1.8 $
 */
public final class PreferencesAction extends Action {
    private static final Logger logger = Logger.getLogger(PreferencesAction.class.getName());

    public final ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        logger.debug("=== PreferencesAction.execute() begin ===");
        ActionsUtil.checkSession(request);
        final HttpSession session = request.getSession();
        final PreferencesForm prefsForm = (PreferencesForm)form;
        final User user = Util.getUser(session);

        final PreferencesProvider pp = (PreferencesProvider)getServlet().getServletContext().getAttribute(Constants.PREFERENCES_PROVIDER);
        final Properties prefs = pp.getPreferences(user, session);


        final String imageUrl = prefsForm.getImageUrl();
        if (prefsForm.getAction() != null) {
            boolean junkThresholdChanged = false;
            boolean vacationMessageChanged = false;

            // Update preferences from the form bean.
            final String username = prefsForm.getUsername();
            if (username == null && prefs.getProperty("user.name") != null) {
                prefs.remove("user.name");
            } else if (username != null && !username.equals(prefs.getProperty("user.name"))) {
                prefs.setProperty("user.name", username);
            }

            final String replyTo = prefsForm.getReplyTo();
            if (replyTo == null && prefs.getProperty("compose.replyTo") != null) {
                prefs.remove("compose.replyTo");
            } else if (replyTo != null && !replyTo.equals(prefs.getProperty("compose.replyTo"))) {
                prefs.setProperty("compose.replyTo", replyTo);
            }

            final String signature = prefsForm.getSignature();
            if (signature == null && prefs.getProperty("compose.signature") != null) {
                prefs.remove("compose.signature");
            } else if (signature != null && !signature.equals(prefs.getProperty("compose.signature"))) {
                prefs.setProperty("compose.signature", signature);
            }

            if (imageUrl == null && prefs.getProperty("compose.X-Image-Url") != null) {
                prefs.remove("compose.X-Image-Url");
            } else if (imageUrl != null && !imageUrl.equals(prefs.getProperty("compose.X-Image-Url"))) {
                prefs.setProperty("compose.X-Image-Url", imageUrl);
            }

            final String junkThreshold = prefsForm.getJunkThreshold();
            if (junkThreshold == null
                    && prefs.getProperty("message.junk.threshold") != null) {
                prefs.remove("message.junk.threshold");
            } else if (junkThreshold != null && !junkThreshold.equals(prefs.getProperty("message.junk.threshold"))) {
                prefs.setProperty("message.junk.threshold", junkThreshold);
                junkThresholdChanged = true;
            }

            final String vacationMessage = prefsForm.getVacationMessage();
            if (vacationMessage == null
                    && prefs.getProperty("vacation.message") != null) {
                prefs.remove("vacation.message");
            } else if (vacationMessage != null && !vacationMessage.equals(prefs.getProperty("vacation.message"))) {
                prefs.setProperty("vacation.message", vacationMessage);
                vacationMessageChanged = true;
            }

            {
                final Session sieveSession = getManageSieveSessionFor(user.getUsername());
                try {
                    Boolean junkSieveEnabled = prefsForm.getJunkSieveEnabled() != null ? prefsForm.getJunkSieveEnabled() : Boolean.FALSE;
                    Boolean vacationSieveEnabled = prefsForm.getVacationSieveEnabled() != null ? prefsForm.getVacationSieveEnabled() : Boolean.FALSE;
                    // Prevent scripts with silly junk threshold.
                    if (Integer.parseInt(junkThreshold) < 1) {
                        junkSieveEnabled =  Boolean.FALSE;
                    }
                    final boolean hasActiveScript = hasActiveGatorMailScript(sieveSession);
                    //final boolean hasJunkScript = isGatorMailJunkScript(sieveSession);
                    //final boolean hasVacationScript = isGatorMailVacationScript(sieveSession);


                    if (junkSieveEnabled.booleanValue() && vacationSieveEnabled.booleanValue()) {
                        //if (junkThresholdChanged || vacationMessageChanged || !hasActiveScript || !hasJunkScript || !hasVacationScript)
                        {
                            final Store store = Util.getMailStore(session);
                            if (!store.isConnected()) {
                                store.connect();
                            }
                            Folder folder = store.getFolder("INBOX");
                            FolderCloserFilter.closeFolder(folder);
                            if (Util.traceProtocol) {
                                Util.addProtocolMarkers(folder, (new Exception()).getStackTrace());
                            }
                            folder = folder.getFolder("Junk");
                            FolderCloserFilter.closeFolder(folder);
                            if (Util.traceProtocol) {
                                Util.addProtocolMarkers(folder, (new Exception()).getStackTrace());
                            }
                            try {
                                if (!folder.exists()) {
                                    folder.create(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS);
                                    folder.setSubscribed(true);
                                    ActionsUtil.flushMailStoreGroupCache(session);
                                }
                            } finally {
                                Util.releaseFolder(folder);
                            }
                            putNewComboSieveScript(sieveSession, folder.getFullName(), Integer.parseInt(junkThreshold), vacationMessage);
                            session.setAttribute("vacationEnabled", Boolean.TRUE);
                        }

                    } else if (junkSieveEnabled.booleanValue()) {
                        //if (junkThresholdChanged || !hasActiveScript || !hasJunkScript)
                        {
                            final Store store = Util.getMailStore(session);
                            if (!store.isConnected()) {
                                store.connect();
                            }
                            Folder folder = store.getFolder("INBOX");
                            folder = folder.getFolder("Junk");
                            try {
                                if (!folder.exists()) {
                                    folder.create(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS);
                                    folder.setSubscribed(true);
                                    ActionsUtil.flushMailStoreGroupCache(session);
                                }
                            } finally {
                                Util.releaseFolder(folder);
                            }
                            putNewJunkSieveScript(sieveSession, folder.getFullName(), Integer.parseInt(junkThreshold));
                            session.setAttribute("vacationEnabled", null);
                        }

                    } else if (vacationSieveEnabled.booleanValue()) {
                        //if (vacationMessageChanged || !hasActiveScript || !hasVacationScript)
                        {
                            putNewVacationSieveScript(sieveSession, vacationMessage);
                            session.setAttribute("vacationEnabled", Boolean.TRUE);
                        }

                    } else if (hasActiveScript) {
                        sieveSession.setActive(null);
                        session.setAttribute("vacationEnabled", null);
                    }

                } finally {
                    sieveSession.logout();
                }
            }

            final Boolean autocomplete = prefsForm.getAutocomplete();
            prefs.setProperty("compose.recipients.autocomplete", autocomplete == null ? "false" : autocomplete.toString());

            final Boolean threading = prefsForm.getThreading();
            if (threading == null && prefs.getProperty("folder.list.threading") != null) {
                prefs.remove("folder.list.threading");
            } else if (threading != null
                    && !threading.equals(
                            Boolean.valueOf(
                                    (prefs.getProperty("folder.list.threading") != null) ? prefs.getProperty("folder.list.threading") : "false"
                            )
                    )
            ) {
                prefs.setProperty("folder.list.threading", threading.toString());
            }

            final Boolean hideHeader = prefsForm.getHideHeader();
            if (hideHeader == null && prefs.getProperty("view.header.hide") != null) {
                prefs.remove("view.header.hide");
            } else if (hideHeader != null
                    && !hideHeader.equals(
                            Boolean.valueOf(
                                    (prefs.getProperty("view.header.hide") != null) ? prefs.getProperty("view.header.hide") : "false"
                            )
                    )
            ) {
                prefs.setProperty("view.header.hide", hideHeader.toString());
            }
        }

        populateFormBeanFromPreferences(prefsForm, prefs, user);
        // Make sure the vacation alert banner is cleared.
        if (prefsForm.getVacationSieveEnabled() != null && prefsForm.getVacationSieveEnabled().booleanValue()) {
            session.setAttribute("vacationEnabled", Boolean.TRUE);
        } else {
            session.setAttribute("vacationEnabled", null);
        }

        if (imageUrl != null) {
            request.setAttribute("X-Image-Url", imageUrl);
        } else {
            request.setAttribute("X-Image-Url", prefs.getProperty("compose.X-Image-Url"));
        }

        logger.debug("=== PreferencesAction.execute() end ===");
        return mapping.findForward("success");
    }

    private void populateFormBeanFromPreferences(final PreferencesForm prefsForm, final Properties prefs, final User user) throws Exception {
        // Populate form bean from perferences.
        prefsForm.setUsername(prefs.getProperty("user.name"));
        prefsForm.setReplyTo(prefs.getProperty("compose.replyTo"));
        prefsForm.setSignature(prefs.getProperty("compose.signature"));
        prefsForm.setImageUrl(prefs.getProperty("compose.X-Image-Url"));
        prefsForm.setJunkThreshold(prefs.getProperty("message.junk.threshold", "5"));
        prefsForm.setVacationMessage(prefs.getProperty("vacation.message", "I am currently unavailable. Your message will be read when I return.\n\nPS: This is an automated response."));
        if (prefs.getProperty("compose.recipients.autocomplete") != null) {
            prefsForm.setAutocomplete(Boolean.valueOf(prefs.getProperty("compose.recipients.autocomplete")));
        } else {
            prefsForm.setAutocomplete(Boolean.TRUE);
        }
        if (prefs.getProperty("folder.list.threading") != null) {
            prefsForm.setThreading(Boolean.valueOf(prefs.getProperty("folder.list.threading")));
        }
        if (prefs.getProperty("view.header.hide") != null) {
            prefsForm.setHideHeader(Boolean.valueOf(prefs.getProperty("view.header.hide")));
        }
        {
            final Session sieveSession = getManageSieveSessionFor(user.getUsername());
            if (hasActiveGatorMailScript(sieveSession)) {
                prefsForm.setJunkSieveEnabled(Boolean.valueOf(isGatorMailJunkScript(sieveSession)));
                prefsForm.setVacationSieveEnabled(Boolean.valueOf(isGatorMailVacationScript(sieveSession)));
            }
            sieveSession.logout();
        }
    }

    private static Session getManageSieveSessionFor(final String username) throws Exception {
        final Context initCtx = new InitialContext();
        final Context envCtx = (Context)initCtx.lookup("java:comp/env");

        Session session = (Session)envCtx.lookup("managesieve/SessionFactory");
        session = session.getSessionFor(username);

        session.authenticate();

        return session;
    }

    private static boolean hasActiveGatorMailScript(final Session session) throws Exception {
        final Response response = session.listScripts();
        final List tokens = response.getTokens();
        final ListIterator lIter = tokens.listIterator();
        while (lIter.hasNext()) {
            final Token token = (Token)lIter.next();
            if (token instanceof Token.ActiveToken) {
                lIter.previous(); // current again
                final Token.StringToken scriptName = (Token.StringToken)lIter.previous(); // previous
                if ("GatorMail".equals(scriptName.getValue())) {
                    return true;
                } else {
                    while (token != lIter.next()) ; // Seek back forward
                }
            }
        }

        return false;
    }

    private static final Pattern junkPattern = Pattern.compile(".*X-UFL-Spam-Level.*", Pattern.DOTALL);
    private static boolean isGatorMailJunkScript(final Session session) throws Exception {
        final Response response = session.getScript("GatorMail");

        if (response.isOK()) {
            final Token t = (Token)response.getTokens().get(0);
            final Token.StringToken st = (Token.StringToken)t;
            final Matcher junkMatcher = junkPattern.matcher(st.getValue());
            return junkMatcher.matches();
        } else {
            System.err.println("repsonse was not OK: "  + response);
        }

        return false;
    }

    private static final Pattern vacationPattern = Pattern.compile(".*vacation.*", Pattern.DOTALL);
    private static boolean isGatorMailVacationScript(final Session session) throws Exception {
        final Response response = session.getScript("GatorMail");

        if (response.isOK()) {
            final Token t = (Token)response.getTokens().get(0);
            final Token.StringToken st = (Token.StringToken)t;
            final Matcher vacationMatcher = vacationPattern.matcher(st.getValue());
            return vacationMatcher.matches();
        } else {
            System.err.println("repsonse was not OK: "  + response);
        }

        return false;
    }

    public static boolean hasActiveVacationScript(final HttpSession session) throws Exception {
        final User user = Util.getUser(session);
        final Session sieveSession = getManageSieveSessionFor(user.getUsername());

        return hasActiveGatorMailScript(sieveSession) && isGatorMailVacationScript(sieveSession);
    }

    private static void putNewComboSieveScript(final Session session, final String folder, final int threshold, final String message) throws Exception {
        if (folder == null) {
            throw new NullPointerException("folder must not be null.");
        }
        if (threshold < 0) {
            throw new IllegalArgumentException("threshold must be zero or larger.");
        }
        if (message == null) {
            throw new NullPointerException("message must not be null.");
        }
        if (message.length() <= 0) {
            throw new IllegalArgumentException("message must not be empty.");
        }

        final StringBuffer sb = new StringBuffer();
        final String CRLF = "\r\n";
        final String stars = "******************************************************".substring(0, threshold);

        sb.append("# This script was generated by GatorMail on ").append(new Date().toString()).append(CRLF);
        sb.append("require \"fileinto\";").append(CRLF);
        sb.append("require \"vacation\";").append(CRLF);
        sb.append("if header :contains \"X-UFL-Spam-Level\" \"").append(stars).append("\" {").append(CRLF);
        sb.append("\tfileinto \"").append(folder).append("\"; ").append(CRLF);
        sb.append("}").append(CRLF);

        sb.append(CRLF);

        sb.append("vacation text:").append(CRLF);

        // dot stuff lines.
        final BufferedReader br = new BufferedReader(new StringReader(message));
        String str;
        while ((str = br.readLine()) != null) {
            if (str.startsWith(".")) {
                sb.append(".");
            }
            sb.append(str).append(CRLF);
        }

        sb.append(".").append(CRLF);
        sb.append(";").append(CRLF);

        session.putScript("GatorMail", sb.toString());
        session.setActive("GatorMail");
    }

    private static void putNewJunkSieveScript(final Session session, final String folder, final int threshold) throws Exception {
        if (folder == null) {
            throw new NullPointerException("folder must not be null.");
        }
        if (threshold < 0) {
            throw new IllegalArgumentException("threshold must be zero or larger.");
        }

        final StringBuffer sb = new StringBuffer();
        final String CRLF = "\r\n";
        final String stars = "******************************************************".substring(0, threshold);

        sb.append("# This script was generated by GatorMail on ").append(new Date().toString()).append(CRLF);
        sb.append("require \"fileinto\";").append(CRLF);
        sb.append("if header :contains \"X-UFL-Spam-Level\" \"").append(stars).append("\" {").append(CRLF);
        sb.append("\tfileinto \"").append(folder).append("\"; ").append(CRLF);
        sb.append("}").append(CRLF);

        session.putScript("GatorMail", sb.toString());
        session.setActive("GatorMail");
    }

    private static void putNewVacationSieveScript(final Session session, final String message) throws Exception {
        if (message == null) {
            throw new NullPointerException("message must not be null.");
        }
        if (message.length() <= 0) {
            throw new IllegalArgumentException("message must not be empty.");
        }

        final StringBuffer sb = new StringBuffer();
        final String CRLF = "\r\n";

        sb.append("# This script was generated by GatorMail on ").append(new Date().toString()).append(CRLF);
        sb.append("require \"vacation\";").append(CRLF);
        sb.append("vacation text:").append(CRLF);

        // dot stuff lines.
        final BufferedReader br = new BufferedReader(new StringReader(message));
        String str;
        while ((str = br.readLine()) != null) {
            if (str.startsWith(".")) {
                sb.append(".");
            }
            sb.append(str).append(CRLF);
        }

        sb.append(".").append(CRLF);
        sb.append(";").append(CRLF);

        session.putScript("GatorMail", sb.toString());
        session.setActive("GatorMail");
    }
}
