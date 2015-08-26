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

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.LookupDispatchAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


/**
 * @author sandymac
 * @author drakee
 * @version $Revision: 1.2 $
 */
public class ModifyFolderAction extends LookupDispatchAction {
    private static final Logger logger = Logger.getLogger(ModifyFolderAction.class.getName());
    private Map map = new HashMap();

    public ModifyFolderAction() {
        map.put("button.delete", "deleteMessages");
        map.put("button.deleteForever", "deleteMessages");
        map.put("button.moveToFolder", "moveToFolder");
        map.put("button.copyToFolder", "copyToFolder");
    }

    /**
     * Provides the mapping from resource key to method name
     *
     * @return                     Resource key / method name map
     */
    protected Map getKeyMethodMap() {
        return map;
    }

    /**
     * Forwards this request to the deletes action.
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
    public ActionForward deleteMessages(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) {
        logger.debug("=== ModifyFolderAction.deleteMessages()...");
        return mapping.findForward("deleteMessages");
    }

    /**
     * Forwards this request to the deletes action.
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
    public ActionForward moveToFolder(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) {
        logger.debug("=== ModifyFolderAction.moveToFolder()...");
        return mapping.findForward("moveMessages");
    }

    /**
     * Forwards this request to the deletes action.
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
    public ActionForward copyToFolder(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) {
        logger.debug("=== ModifyFolderAction.copyToFolder()...");
        return mapping.findForward("copyMessages");
    }
}
