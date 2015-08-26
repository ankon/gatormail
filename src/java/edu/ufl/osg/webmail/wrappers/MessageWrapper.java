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

package edu.ufl.osg.webmail.wrappers;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;


/**
 * Wrapper for a JavaMail Message so that the properties follow the actuall
 * JavaBean pattern.
 *
 * @author sandymac
 * @version $Revision: 1.2 $
 */
public class MessageWrapper {
    /**
     * Holds value of property javaMailMessage.
     */
    private Message javaMailMessage;

    /**
     * Creates a new instance of MessageWrapper
     */
    public MessageWrapper(final Message message) {
        setJavaMailMessage(message);
    }

    /**
     * Getter for property javaMailMessage.
     *
     * @return                     Value of property javaMailMessage.
     */
    public Message getJavaMailMessage() {
        return this.javaMailMessage;
    }

    /**
     * Setter for property javaMailMessage.
     *
     * @param javaMailMessage     New value of property javaMailMessage.
     */
    protected void setJavaMailMessage(final Message javaMailMessage) {
        this.javaMailMessage = javaMailMessage;
    }

    /**
     * Getter for property subject.
     *
     * @return                     Value of property subject.
     */
    public String getSubject() throws MessagingException {
        return javaMailMessage.getSubject();
    }

    /**
     * Getter for property to.
     *
     * @return                     {@link java.util.List} of "to" recipients,
     *                             empty list if no recipients.
     */
    public List getTo() throws MessagingException {
        final List list;

        final Address[] addresses = javaMailMessage.getRecipients(RecipientType.TO);
        if (addresses != null) {
            list = Arrays.asList(addresses);
        } else {
            list = Collections.EMPTY_LIST;
        }
        return list;
    }

    /**
     * Getter for property from.
     *
     * @return                     {@link java.util.List} of senders, empty list
     *                             if no senders.
     */
    public List getFrom() throws MessagingException {
        final List list;

        final Address[] addresses = getJavaMailMessage().getFrom();
        if (addresses != null) {
            list = Arrays.asList(addresses);
        } else {
            list = Collections.EMPTY_LIST;
        }
        return list;
    }

    /**
     * Getter for property size.
     *
     * @return                     Value of property size.
     */
    public int getSize() throws MessagingException {
        return getJavaMailMessage().getSize();
    }
}
