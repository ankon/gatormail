/*
 * $Header: /var/tmp/gatormail/cvs/GatorMail/src/java/edu/ufl/osg/webmail/tags/FormatWriteTag.java,v 1.3 2004/02/23 01:05:34 sandymac Exp $
 * $Revision: 1.3 $
 * $Date: 2004/02/23 01:05:34 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002-2004 William A. McArthur, Jr.
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

import org.apache.struts.taglib.bean.WriteTag;
import org.apache.struts.util.RequestUtils;

import javax.servlet.jsp.JspException;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Tag that retrieves the specified property of the specified bean, converts it
 * to a String representation (if necessary), and writes it to the current
 * output stream, optionally filtering characters that are sensitive in HTML.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2004/02/23 01:05:34 $
 */
public class FormatWriteTag extends WriteTag {
    /** The format class to be instantiated */
    protected String formatClass = null;

    /** The key to search format class in application resources */
    protected String formatClassKey = null;

    public String getFormatClass() {
        return formatClass;
    }

    public void setFormatClass(final String formatClass) {
        this.formatClass = formatClass;
    }

    public String getFormatClassKey() {
        return formatClassKey;
    }

    public void setFormatClassKey(final String formatClassKey) {
        this.formatClassKey = formatClassKey;
    }

    /**
     * Format value according to specified format string (as tag attribute or
     * as string from message resources) or to current user locale.
     *
     * @param valueToFormat value to process and convert to String
     *
     * @exception JspException if a JSP exception has occurred
     */
    protected String formatValue(final Object valueToFormat) throws JspException {
        Format format = null;
        final Object value = valueToFormat;
        final Locale locale = RequestUtils.retrieveUserLocale(pageContext, this.localeKey);
        boolean formatStrFromResources = false;
        String formatString = formatStr;

        if ((formatClass != null) || (formatClassKey != null)) {
            try {
                final Class clazz;
                final Constructor clazzConstructor;
                final Class[] paramTypes;
                final Object[] params;

                // Try to retrieve format string from resources by the key from formatKey.
                if ((formatString == null) && (formatKey != null)) {
                    formatString = retrieveFormatString(this.formatKey);

                    if (formatString != null) {
                        formatStrFromResources = true;
                    }
                }

                // Try to retrieve format string from resources by the key from formatKey.
                if ((formatClass == null) && (formatClassKey != null)) {
                    formatClass = retrieveFormatString(this.formatClassKey);
                }

                clazz = Class.forName(formatClass);

                if (formatString == null) {
                    paramTypes = new Class[0];
                    params = new Object[0];
                } else {
                    if (!formatStrFromResources) {
                        paramTypes = new Class[1];
                        params = new Object[1];
                    } else {
                        paramTypes = new Class[2];
                        params = new Object[2];

                        paramTypes[1] = Locale.class;
                        params[1] = locale;
                    }

                    paramTypes[0] = String.class;

                    params[0] = formatString;
                }

                clazzConstructor = clazz.getConstructor(paramTypes);
                format = (Format)clazzConstructor.newInstance(params);
            } catch (Exception e) {
                e.printStackTrace();

                // TODO: What message should this be
                final JspException jE = new JspException(messages.getMessage("write.format", formatString), e);
                RequestUtils.saveException(pageContext, jE);
                throw jE;
            }
        } else {
            // Return String object as is.
            if (value instanceof String) {
                return (String)value;
            } else {
                // Try to retrieve format string from resources by the key from formatKey.
                if ((formatString == null) && (formatKey != null)) {
                    formatString = retrieveFormatString(this.formatKey);

                    if (formatString != null) {
                        formatStrFromResources = true;
                    }
                }

                // Prepare format object for numeric values.
                if (value instanceof Number) {
                    if (formatString == null) {
                        if ((value instanceof Byte) || (value instanceof Short) || (value instanceof Integer) || (value instanceof Long) || (value instanceof BigInteger)) {
                            formatString = retrieveFormatString(INT_FORMAT_KEY);
                        } else if ((value instanceof Float) || (value instanceof Double) || (value instanceof BigDecimal)) {
                            formatString = retrieveFormatString(FLOAT_FORMAT_KEY);
                        }

                        if (formatString != null) {
                            formatStrFromResources = true;
                        }
                    }

                    if (formatString != null) {
                        try {
                            format = NumberFormat.getNumberInstance(locale);

                            if (formatStrFromResources) {
                                ((DecimalFormat)format).applyLocalizedPattern(formatString);
                            } else {
                                ((DecimalFormat)format).applyPattern(formatString);
                            }
                        } catch (IllegalArgumentException _e) {
                            final JspException e = new JspException(messages.getMessage("write.format", formatString));
                            RequestUtils.saveException(pageContext, e);
                            throw e;
                        }
                    }
                } else if (value instanceof Date) {
                    if (formatString == null) {
                        if (value instanceof Timestamp) {
                            formatString = retrieveFormatString(SQL_TIMESTAMP_FORMAT_KEY);
                        } else if (value instanceof java.sql.Date) {
                            formatString = retrieveFormatString(SQL_DATE_FORMAT_KEY);
                        } else if (value instanceof Time) {
                            formatString = retrieveFormatString(SQL_TIME_FORMAT_KEY);
                        } else if (value instanceof Date) {
                            formatString = retrieveFormatString(DATE_FORMAT_KEY);
                        }

                        if (formatString != null) {
                            formatStrFromResources = true;
                        }
                    }

                    if (formatString != null) {
                        if (formatStrFromResources) {
                            format = new SimpleDateFormat(formatString, locale);
                        } else {
                            format = new SimpleDateFormat(formatString);
                        }
                    }
                }
            }
        }

        if (format != null) {
            return format.format(value);
        } else {
            return value.toString();
        }
    }

    public int doEndTag() throws JspException {
        /*
        This is to fix the problem with TagPooling. Because of the way the
        release() method is NOT guarentteed to be called between use of pooled
        tags the following if could do the wrong behavior if there is a stale
        value in formatClass and a new value in formatClassKey.

        if ((formatClass != null) || (formatClassKey != null))

        This one was really obscure for me. - Sandy
        */
        final int r = super.doEndTag();
        formatClass = null;
        formatClassKey = null;
        return r;
    }

    /**
     * Release all allocated resources.
     */
    public void release() {
        super.release();
        formatClass = null;
        formatClassKey = null;
    }
}