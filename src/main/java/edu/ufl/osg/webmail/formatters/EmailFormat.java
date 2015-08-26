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

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.StringTokenizer;


/**
 * <p>Takes a rfc822 email address and reformats it's a few different ways. This
 * uses {@link java.text.MessageFormat} internally and following parameters are
 * assigned to:</p>
 *
 * <ul>
 *
 * <li>{0} - {@link javax.mail.internet.InternetAddress#toString()}</li> <li>{1}
 * - {@link javax.mail.internet.InternetAddress#getPersonal()}</li> <li>{2} -
 * {@link javax.mail.internet.InternetAddress#getAddress()}</li>
 *
 * </ul>
 *
 * <p>Note that each part is filted with {@link
 * org.apache.struts.util.ResponseUtils#filter(java.lang.String)} before it is
 * used so there is no need to filter at the taglib level.</p>
 *
 * @author sandymac
 * @version $Revision: 1.4 $
 * @see    java.text.MessageFormat
 */
public class EmailFormat extends Format {
    private String params = "<span title=\"{2}\">{1}</span>";

    /**
     * Creates a new instance of EmailFormat
     */
    public EmailFormat() {
        super();
    }

    /**
     * Creates a new instance of EmailFormat
     */
    public EmailFormat(final String params) {
        this();
        setParams(params);
    }

    /**
     * Creates a new instance of EmailFormat
     */
    public EmailFormat(final String params, final Locale locale) {
        this(params);
    }

    /**
     * Formats an object and appends the resulting text to a given string
     * buffer. If the <code>pos</code> argument identifies a field used by the
     * format, then its indices are set to the beginning and end of the first
     * such field encountered.
     *
     * @param     obj                 The object to format
     * @param     toAppendTo          where the text is to be appended
     * @param     pos                 A <code>FieldPosition</code> identifying a
     *                                field in the formatted text
     * @return                        the string buffer passed in as
     *                                <code>toAppendTo</code>, with formatted
     *                                text appended
     * @exception NullPointerException if
     *                                <code>toAppendTo</code> or
     *                                <code>pos</code> is null
     * @exception IllegalArgumentException if the Format
     *                                cannot format the given object
     */
    public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) {
        final String string;
        final InternetAddress address;
        String name;
        String email;
        final String format = getParams();

        if (obj instanceof String) {
            string = (String)obj;
        } else {
            string = obj.toString();
        }

        try {
            address = new InternetAddress(string);
        } catch (AddressException e) {
            // There was a problem, just filter and return it
            // prevent the string from being too long.
            String s = string;
            if (s != null && s.length() > 35) {
                s = s.substring(0, 35);
            }
            return toAppendTo.append(ResponseUtils.filter(s));
        }

        name = address.getPersonal();
        email = address.getAddress();

        try {
            // If there is no name part
            if (name == null || name.length() == 0) {
                name = email;

                // If the name (which is an address) is real long
                if (name.length() > 30) {
                    String left = name.substring(0, name.indexOf("@"));
                    final String right = name.substring(name.indexOf("@") + 1, name.length());

                    if (left.length() > 25) {
                        left = left.substring(0, 20) + "...";
                    }
                    name = left + "@";

                    final StringTokenizer st = new StringTokenizer(right, ".");

                    while (st.hasMoreTokens()) {
                        name += (st.nextToken().substring(0, 1) + ".");
                    }

                    name = name.substring(0, name.length() - 1);
                }
            }
        } catch (Throwable t) {
            // reset
            name = address.getPersonal();
            email = address.getAddress();
        }


        final String[] args = new String[3];
        args[0] = ResponseUtils.filter(address.toString());
        args[1] = ResponseUtils.filter(name);
        args[2] = ResponseUtils.filter(email);

        return toAppendTo.append(MessageFormat.format(format, args));

    }

    /**
     * Parses text from a string to produce an object.
     *
     * <p>
     * The method attempts to parse text starting at the index given by
     * <code>pos</code>. If parsing succeeds, then the index of <code>pos</code>
     * is updated to the index after the last character used (parsing does not
     * necessarily use all characters up to the end of the string), and the
     * parsed object is returned. The updated <code>pos</code> can be used to
     * indicate the starting point for the next call to this method. If an error
     * occurs, then the index of <code>pos</code> is not changed, the error
     * index of <code>pos</code> is set to the index of the character where the
     * error occurred, and null is returned. </p>
     *
     * @param     source              A <code>String</code>, part of which
     *                                should be parsed.
     * @param     pos                 A <code>ParsePosition</code> object with
     *                                index and error index information as
     *                                described above.
     * @return                        An <code>Object</code> parsed from the
     *                                string. In case of error, returns null.
     * @exception NullPointerException if <code>pos</code>
     *                                is null.
     */
    public Object parseObject(final String source, final ParsePosition pos) {
        return null;
    }

    public String getParams() {
        return params;
    }

    public void setParams(final String params) {
        this.params = params;
    }
}
