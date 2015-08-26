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

package edu.ufl.osg.webmail.tags;

import edu.ufl.osg.webmail.Constants;
import org.apache.struts.taglib.logic.ForwardTag;
import org.apache.struts.taglib.logic.NotPresentTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * Checks that user is logged in else forwards the user to the "login" action
 * mapping.
 *
 * @author sandymac
 * @version $Revision: 1.2 $
 */
public class CheckLogin extends TagSupport {
    private static String NOTPRESENT_TAG_NAME = Constants.USER_KEY;
    private static String NOTPRESENT_TAG_SCOPE = "session";
    private static String FORWARD_TAG_NAME = "login";
    private NotPresentTag notPresentTag = new NotPresentTag();
    private ForwardTag forwardTag = new ForwardTag();

    /**
     * Creates a new instance of CheckLogin
     */
    public CheckLogin() {
    }

    /**
     * Defer processing until the end of this tag is encountered.
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        notPresentTag.setPageContext(pageContext);
        notPresentTag.setParent(this);
        notPresentTag.setName(NOTPRESENT_TAG_NAME);
        notPresentTag.setScope(NOTPRESENT_TAG_SCOPE);

        try {
            final int nptResult = notPresentTag.doStartTag();

            if (nptResult != SKIP_BODY) {
                forwardTag.setPageContext(pageContext);
                forwardTag.setParent(notPresentTag);
                forwardTag.setName(FORWARD_TAG_NAME);

                try {
                    forwardTag.doStartTag();

                    if (forwardTag.doEndTag() == SKIP_PAGE) {
                        return SKIP_PAGE;
                    }
                } finally {
                    forwardTag.release();
                }
            }

            return notPresentTag.doEndTag();
        } finally {
            notPresentTag.release();
        }
    }
}