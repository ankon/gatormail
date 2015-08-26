/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002, 2003 William A. McArthur, Jr.
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

import com.opensymphony.oscache.web.ServletCache;
import edu.ufl.osg.webmail.Constants;
import edu.ufl.osg.webmail.User;
import edu.ufl.osg.webmail.data.AttachDAO;
import edu.ufl.osg.webmail.data.AttachList;
import edu.ufl.osg.webmail.data.DAOFactory;
import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.mail.Folder;
import javax.mail.Service;
import javax.mail.Store;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.ref.Reference;
import java.util.Date;
import java.util.Enumeration;


/**
 * Logout controller.
 *
 * @author sandymac
 * @version $Revision: 1.2 $
 */
public class LogoutAction extends Action {
    private static final Logger logger = Logger.getLogger(LogoutAction.class.getName());

    /**
     * Logs the user out by closing any JavaMail objects on the servlet session
     * and then invalidates their session.
     *
     * @param  mapping             The ActionMapping used to select this
     *                             instance
     * @param  form                The optional ActionForm bean for this request
     *                             (if any)
     * @param  request             The HTTP request we are processing
     * @param  response            The HTTP response we are creating
     * @return                     An ActionForward back to the login page.
     * @throws Exception           if the application business logic throws an
     *                             exception
     */
    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final HttpSession session = request.getSession();

        // clear any attachments the user may still have in storage:
        final User user = Util.getUser(session);
        if (user != null) { // it's ok, he's logging out anyway
            logger.info(user.getUsername() + " is logging out.");
            final AttachList attachList = (AttachList)session.getAttribute(Constants.ATTACHMENT_LIST);
            if (attachList != null) {
                final AttachDAO attachDAO = DAOFactory.getInstance().getAttachDAO();
                attachDAO.clearAttachList(attachList);
            }
        }

        synchronized (session) {
            // XXX: Should I catch any exceptions and silently ignore them here?
            try {
                final Enumeration attribNames = session.getAttributeNames();

                while (attribNames.hasMoreElements()) {
                    final String attributeName = (String)attribNames.nextElement();

                    Object o = session.getAttribute(attributeName);

                    // Get past any SoftReferences
                    if (o instanceof Reference) {
                        o = ((Reference)o).get();
                    }

                    try {
                        //Close any JavaxMail Service and Folder objects
                        if (o instanceof Service) {
                            final Service s = (Service)o;

                            if (s.isConnected()) {
                                s.close();
                            }
                        } else if (o instanceof Folder) {
                            final Folder f = (Folder)o;
                            if (Util.traceProtocol) {
                                Util.addProtocolMarkers(f, (new Exception()).getStackTrace());
                            }

                            if (f.isOpen()) {
                                f.close(false);
                            }
                        } else if (o instanceof User) {
                            final User u = (User)o;
                            Store store = u.getMailStore();
                            if (store.isConnected()) {
                                store.close();
                            }

                        } else if (o instanceof ServletCache) {
                            ServletCache cache = (ServletCache)o;
                            cache.flushAll(new Date());
                        }
                    } catch (Exception e) {
                        logger.error("Silently ignoring Exception during logout.", e);
                    }
                }
            } finally {
                // Empty the session
                try {
                    session.invalidate();
                } catch (IllegalStateException ise) {
                    // swallow
                }
            }
        }

        return mapping.findForward("success");
    }
}
