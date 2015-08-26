/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002, 2003 William A. McArthur, Jr.
 * Copyright (C) 2003 The Open Systems Group / University of Florida
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

import edu.ufl.osg.webmail.Constants;
import edu.ufl.osg.webmail.data.AddressList;
import edu.ufl.osg.webmail.data.ConfigDAO;
import edu.ufl.osg.webmail.data.DAOFactory;
import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Readies view for selecting addresses for saving in the addressbook.
 *
 * @author drakee
 * @version $Revision: 1.2 $
 */
public class SelectAddressesAction extends Action {
    private static final Logger logger = Logger.getLogger(SelectAddressesAction.class.getName());

    /**
     *
     *
     * @param     mapping             The ActionMapping used to select this
     *                                instance
     * @param     form                The optional ActionForm bean for this
     *                                request (if any)
     * @param     request             The HTTP request we are processing
     * @param     response            The HTTP response we are creating
     * @exception Exception if the application business
     *                                logic throws an exception
     */
    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        logger.debug("=== SelectAddressesAction.execute() begin ===");
        ActionsUtil.checkSession(request);

        final ActionErrors errors = new ActionErrors();
        final HttpSession session = request.getSession();
        final AddressList addressList = Util.getAddressList(session);

        // check that there won't be too many entries in the addressbook
        final ConfigDAO configDAO = DAOFactory.getInstance().getConfigDAO();
        final String maxsizeStr = configDAO.getProperty("maxsizeAddressbook");
        final int maxsize = Integer.parseInt(maxsizeStr);
        final int size = addressList.size();
        logger.debug("address book size: " + size + ". limit: " + maxsize);
        if (size >= maxsize) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.address.overlimit", maxsizeStr));
            saveErrors(request, errors);
            return mapping.findForward("fail");
        }

        // Used by address book quota message
        request.setAttribute(Constants.ADDRESSBK_LIMIT, String.valueOf(maxsize));
        request.setAttribute(Constants.ADDRESSBK_USAGE, String.valueOf(size));

        // Open the folder
        final Folder folder = ActionsUtil.fetchFolder(form, request);
        request.setAttribute("folderFullName", folder.getFullName());

        // Find the message
        final Message message = ActionsUtil.fetchMessage(form, folder);

        final Map messageParams = new HashMap();
        Util.addMessageParams(message, messageParams);
        request.setAttribute("messageParams", messageParams);

        final List saveAddressList = new ArrayList();
        setSaveAddressList(saveAddressList, message);

        Util.releaseFolder(folder); // clean up

        request.setAttribute(Constants.SAVE_ADDRESS_LIST, saveAddressList);

        logger.debug("=== SelectAddressesAction.execute() end ===");
        return mapping.findForward("success");
    }

    private static void setSaveAddressList(final List list, final Message message) throws MessagingException, IOException {
        final Address[] replyToAddresses = message.getReplyTo();
        addToList(list, replyToAddresses);
        final Address[] recipientAddresses = message.getAllRecipients();
        addToList(list, recipientAddresses);

        final String contentType = message.getContentType();
        logger.debug("contentType: " + contentType);
        if (message.isMimeType("multipart/*")) {
            final Multipart multipart = (Multipart)message.getContent();
            final int count = multipart.getCount();
            for (int i = 0; i < count; i++) {
                final Part part = multipart.getBodyPart(i);
                if (part.isMimeType("message/*")) {
                    setSaveAddressList(list, (Message)part.getContent());
                } else if (part.isMimeType("multipart/*")) {
                    setSaveAddressList(list, (Multipart)part.getContent());
                }
            }
        } else if (message.isMimeType("message/*")) {
            setSaveAddressList(list, (Message)message.getContent());
        }
    }

    private static void setSaveAddressList(final List list, final Multipart multipart) throws MessagingException, IOException {
        final int count = multipart.getCount();
        for (int i = 0; i < count; i++) {
            final Part part = multipart.getBodyPart(i);
            if (part.isMimeType("message/*")) {
                setSaveAddressList(list, (Message)part.getContent());
            } else if (part.isMimeType("multipart/*")) {
                setSaveAddressList(list, (Multipart)part.getContent());
            }
        }
    }

    private static void addToList(final List list, final Address[] addresses) {
        if (addresses != null) {
            for (int i = 0; i < addresses.length; i++) {
                if (!list.contains(addresses[i])) {
                    logger.debug("adding " + addresses[i] + " to selectAddresses list");
                    list.add(addresses[i]);
                }
            }
        }
    }
}
