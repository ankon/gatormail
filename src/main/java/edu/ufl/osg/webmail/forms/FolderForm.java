/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002, 2003 William A. McArthur, Jr.
 * Copyright (C) 2003-2005 The Open Systems Group / University of Florida
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

package edu.ufl.osg.webmail.forms;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Basic Folder view form. This is extended by a number of other forms.
 *
 * @author sandymac
 * @version $Revision: 1.6 $
 */
public class FolderForm extends ActionForm {
    private static final Logger logger = Logger.getLogger(FolderForm.class.getName());
    private static final Integer ZERO = new Integer(0);
    private static final Integer ONE = new Integer(1);
    private static final String DATEDN = (String)"dateDN";

    /**
     * Holds value of property folder.
     */
    private String folder;

    /**
     * Holds value of property folder.
     */
    private String sort = DATEDN;

    /**
     * Simple message filter to be applies to this folder.
     */
    private String filter = "";

    /**
     * What type of filtering are we doing.
     */
    private String filterType = null;

    private Integer page = ONE;

    private int size = 0;

    /**
     * Resets the <code>ActionForm</code> and then resets <code>folder</code> to
     * <code>null</code> and the filter to an empty string.
     *
     * @param mapping             The mapping used to select this instance
     * @param request             The servlet request we are processing
     */
    public void reset(final ActionMapping mapping, final HttpServletRequest request) {
        super.reset(mapping, request);
        setFolder(null);
        setFilter("");
        setFilterType(null);
        setSort(DATEDN);
        setPage(ONE);
    }

    /**
     * Calls {@link FormsUtil#validateFolder(String, ActionErrors)}.
     *
     * @param  mapping             The mapping used to select this instance
     * @param  request             The servlet request we are processing
     * @return                     Errors, if any are found.
     * @see
     *                             FormsUtil#validateFolder(String,ActionErrors)
     */
    public ActionErrors validate(final ActionMapping mapping, final HttpServletRequest request) {
        final ActionErrors errors = new ActionErrors();

        FormsUtil.validateFolder(getFolder(), errors);

        return errors;
    }

    /**
     * Getter for property folder.
     *
     * @return                     Value of property folder.
     */
    public String getFolder() {
        return this.folder;
    }

    public String getSort() {
        return this.sort;
    }

    /**
     * Setter for property folder.
     *
     * @param folder              New value of property folder.
     */
    public void setFolder(final String folder) {
        this.folder = folder;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        if (filter == null) {
            filter = "";
        }
        this.filter = filter.trim();
    }

    /**
     * An alias for getFolder().
     *
     * @return                     Value of property fullName.
     */
    public String getFullName() {
        return getFolder();
    }

    /**
     * An alias for setFolder(String).
     *
     * @param fullName            New value of property fullName.
     */
    public void setFullName(final String fullName) {
        setFolder(fullName);
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(final String filterType) {
        this.filterType = filterType;
    }

    public Integer getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public void setPage(final Integer page) {
        this.page = page;
    }

    public void setSize(final int size) {
        this.size = size;
    }
}
