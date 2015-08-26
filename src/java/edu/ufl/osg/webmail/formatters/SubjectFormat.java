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

package edu.ufl.osg.webmail.formatters;

import org.apache.struts.util.ResponseUtils;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;


/**
 * @author sandymac
 * @version $Revision: 1.2 $
 */
public class SubjectFormat extends Format {
    /** Holds value of property params. */
    private int truncLength = 65;

    /**
     * Creates a new instance of SubjectFormat
     */
    public SubjectFormat() {
        super();
    }

    /**
     * Creates a new instance of SubjectFormat
     */
    public SubjectFormat(final String params) {
        this();
        setParams(params);
    }

    /**
     * Creates a new instance of SubjectFormat
     */
    public SubjectFormat(final String params, final Locale locale) {
        this(params);
    }

    /**
     * Formats an object and appends the resulting text to a given string
     * buffer. If the <code>pos</code> argument identifies a field used by the
     * format, then its indices are set to the beginning and end of the first
     * such field encountered.
     *
     * @param obj The object to format
     * @param toAppendTo where the text is to be appended
     * @param pos A <code>FieldPosition</code> identifying a field in the
     *        formatted text
     *
     * @return the string buffer passed in as <code>toAppendTo</code>, with
     *         formatted text appended
     *
     * @exception NullPointerException if <code>toAppendTo</code> or
     *            <code>pos</code> is null
     * @exception IllegalArgumentException if the Format cannot format the
     *            given object
     */
    public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) {
        String subject = (String)obj;
        String truncSubject = subject;

        subject = subject.trim();

        if (subject.length() > truncLength) {
            truncSubject = subject.substring(0, truncLength) + "...";
        }

        toAppendTo.append("<span title=\"");
        toAppendTo.append(ResponseUtils.filter(subject));
        toAppendTo.append("\">");
        toAppendTo.append(ResponseUtils.filter(truncSubject));
        toAppendTo.append("</span>");

        return toAppendTo;
    }

    /**
     * Parses text from a string to produce an object.
     *
     * <p>
     * The method attempts to parse text starting at the index given by
     * <code>pos</code>. If parsing succeeds, then the index of
     * <code>pos</code> is updated to the index after the last character used
     * (parsing does not necessarily use all characters up to the end of the
     * string), and the parsed object is returned. The updated
     * <code>pos</code> can be used to indicate the starting point for the
     * next call to this method. If an error occurs, then the index of
     * <code>pos</code> is not changed, the error index of <code>pos</code> is
     * set to the index of the character where the error occurred, and null is
     * returned.
     * </p>
     *
     * @param source A <code>String</code>, part of which should be parsed.
     * @param pos A <code>ParsePosition</code> object with index and error
     *        index information as described above.
     *
     * @return An <code>Object</code> parsed from the string. In case of error,
     *         returns null.
     *
     * @exception NullPointerException if <code>pos</code> is null.
     */
    public Object parseObject(final String source, final ParsePosition pos) {
        return null;
    }

    /**
     * Getter for property params.
     *
     * @return Value of property params.
     */
    public String getParams() {
        return Integer.toString(truncLength);
    }

    /**
     * Setter for property params.
     *
     * @param params New value of property params.
     */
    public void setParams(final String params) {
        truncLength = Integer.parseInt(params);
    }
}