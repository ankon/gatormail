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
import edu.ufl.osg.webmail.beans.ResultBean;
import edu.ufl.osg.webmail.data.AddressList;
import edu.ufl.osg.webmail.data.ConfigDAO;
import edu.ufl.osg.webmail.data.DAOFactory;
import edu.ufl.osg.webmail.forms.AddressesForm;
import edu.ufl.osg.webmail.util.Util;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves a list of addresses into a user's address book.
 *
 * @author drakee
 * @version $Revision: 1.4 $
 */
public class SaveAddressesAction extends Action {
    private static final Logger logger = Logger.getLogger(SaveAddressesAction.class.getName());

    /**
     * Saves an addressbook entry.
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
        logger.debug("=== SaveAddressesAction.execute() begin ===");
        ActionsUtil.checkSession(request);

        final ActionErrors errors = new ActionErrors();
        final HttpSession session = request.getSession();

        // grab info from form
        final AddressesForm addressesForm = (AddressesForm)form;
        final String[] email = addressesForm.getEmail();
        final String[] name = addressesForm.getName();

        // get current address book from session
        final AddressList addressList = Util.getAddressList(session);

        // go ahead and add the entry
        final List newAddressList = new ArrayList();
        final String[] values = request.getParameterValues(Constants.IS_SELECTED);
        for (int i = 0; i < values.length; i++) {
            final int value = Integer.parseInt(values[i]);
            String address = email[value];
            address = address != null ? address.trim() : null;
            String personal = name[value];
            personal = personal != null ? personal.trim() : null;
            final InternetAddress iAddress = new InternetAddress(address, personal);
            if (addressList.contains(iAddress)) {
                logger.error("addressList already contains " + iAddress);
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.address.duplicate", iAddress.toString()));
                saveErrors(request, errors);
                return mapping.findForward("fail");
            } else {
                newAddressList.add(iAddress);
            }
        }

        // check that there won't be too many entries in the addressbook
        final ConfigDAO configDAO = DAOFactory.getInstance().getConfigDAO();
        final String maxsizeStr = configDAO.getProperty("maxsizeAddressbook");
        final int maxsize = Integer.parseInt(maxsizeStr);
        final int size = addressList.size();
        final int newSize = newAddressList.size();
        logger.debug("new entries: " + newSize);
        if (size + newSize >= maxsize) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.saveAddresses.overlimit", maxsizeStr, String.valueOf(maxsize - size)));
            saveErrors(request, errors);
            return mapping.findForward("fail");
        }

        for (int i = 0; i < newSize; i++) {
            final InternetAddress iAddress = (InternetAddress)newAddressList.get(i);
            // add entry to both in-memory list and backend storage
            addressList.addAddress(iAddress);
            logger.debug("saved entry " + i + " to addressbook.");
        }

        // set success message for the upcoming view
        final ResultBean result = new ResultBean();
        if (newSize > 1) {
            result.setMessage(Util.getFromBundle("saveAddresses.result.success.multiple"), String.valueOf(newSize));
        } else {
            result.setMessage(Util.getFromBundle("saveAddresses.result.success.single"));
        }
        request.setAttribute(Constants.RESULT, result);

        logger.debug("=== SaveAddressesAction.execute() end ===");
        return mapping.findForward("success");
    }
}
