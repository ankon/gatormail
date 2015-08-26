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
import edu.ufl.osg.webmail.forms.AddressForm;
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

/**
 * Deletes one or more addressbook entries.
 *
 * @author drakee
 * @version $Revision: 1.2 $
 */
public class DeleteAddressAction extends Action {
    private static final Logger logger = Logger.getLogger(DeleteAddressAction.class.getName());

    /**
     * Delete addressbook entry or entries.
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
        logger.debug("=== DeleteAddressAction.execute() begin ===");
        ActionsUtil.checkSession(request);

        final ActionErrors errors = new ActionErrors();
        final AddressForm addressForm = (AddressForm)form;
        final String email = addressForm.getEmail();
        final String name = addressForm.getName();

        final HttpSession session = request.getSession();
        final AddressList addressList = Util.getAddressList(session);
        final InternetAddress internetAddress = new InternetAddress(email, name);

        // search and destroy
        if (addressList.containsEmail(email)) {
            // remove in memory and on disk
            addressList.removeAddress(internetAddress);
            logger.debug("removed from addressbook. entry: " + internetAddress);

            // store the success message
            final ResultBean result = new ResultBean(Util.getFromBundle("deleteAddress.result.success"));
            request.setAttribute(Constants.RESULT, result);
        } else { // address entry doesn't exist
            final ActionError error = new ActionError("error.deleteAddress.notexist", internetAddress.toString());
            errors.add(ActionErrors.GLOBAL_ERROR, error);
            saveErrors(request, errors);
            return mapping.findForward("fail");
        }

        logger.debug("=== DeleteAddressAction.execute() end ===");
        return mapping.findForward("success");
    }
}