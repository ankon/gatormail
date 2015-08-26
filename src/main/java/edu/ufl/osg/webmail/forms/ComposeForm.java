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

package edu.ufl.osg.webmail.forms;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

import javax.servlet.http.HttpServletRequest;

/**
 * Holds values required for composing, replying to, forwarding, and sending a message.
 *
 * @author sandymac
 * @version $Revision: 1.2 $
 */
public final class ComposeForm extends MessageForm {
    /** Holds value of property subject. */
    private String subject;

    /** Holds value of property to. */
    private String to;

    /** Holds value of property body. */
    private String body;

    /** Holds value of property cc. */
    private String cc;

    /** Holds value of property bcc. */
    private String bcc;

    /** Holds value of property action. */
    private String action;

    /** FormFile is the struts representation of an uploaded file */
    private FormFile attachment;

    /** Copy the message to Sent folder when sending it? */
    private boolean copyToSent = false;

    /** Holds value of composeKey. It is the unique key for this compose session for a user. */
    private String composeKey;

    /**
     * Resets the <code>FolderForm</code> and then resets TODO???.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(final ActionMapping mapping, final HttpServletRequest request) {
        super.reset(mapping, request);
        setAttachment(null);
        // XXX Does this need to be flushed out?
        // TODO Yes it should be BUT we make assumptions that prevent this from happening.
        // ModifyComposeAction and SentAction need to know how to correctly communicate
    }

    /**
     * Calls the super class's validate method (validates that a message and its
     * folder is specified).
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     *
     * @return Errors, if any are found.
     */
    public ActionErrors validate(final ActionMapping mapping, final HttpServletRequest request) {
        final ActionErrors errors = super.validate(mapping, request);
        return errors;
    }

    /**
     * Getter for property subject.
     *
     * @return Value of property subject.
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * Setter for property subject.
     *
     * @param subject New value of property subject.
     */
    public void setSubject(final String subject) {
        this.subject = subject;
    }

    /**
     * Getter for property to.
     *
     * @return Value of property to.
     */
    public String getTo() {
        return this.to;
    }

    /**
     * Setter for property to.
     *
     * @param to New value of property to.
     */
    public void setTo(final String to) {
        this.to = to;
    }

    /**
     * Getter for property body.
     *
     * @return Value of property body.
     */
    public String getBody() {
        return this.body;
    }

    /**
     * Setter for property body.
     *
     * @param body New value of property body.
     */
    public void setBody(final String body) {
        this.body = body;
    }

    /**
     * Getter for property cc.
     *
     * @return Value of property cc.
     */
    public String getCc() {
        return this.cc;
    }

    /**
     * Setter for property cc.
     *
     * @param cc New value of property cc.
     */
    public void setCc(final String cc) {
        this.cc = cc;
    }

    /**
     * Getter for property bcc.
     *
     * @return Value of property bcc.
     */
    public String getBcc() {
        return this.bcc;
    }

    /**
     * Setter for property bcc.
     *
     * @param bcc New value of property bcc.
     */
    public void setBcc(final String bcc) {
        this.bcc = bcc;
    }

    /**
     * Getter for property attachment.
     *
     * @return Value of property attachment.
     */
    public FormFile getAttachment() {
        return this.attachment;
    }

    /**
     * Setter for property attachment.
     *
     * @param attachment New value of property attachment.
     */
    public void setAttachment(final FormFile attachment) {
        this.attachment = attachment;
    }

    /**
     * Getter for property copyToSent.
     *
     * @return Value of property copyToSent.
     */
    public boolean isCopyToSent() {
        return copyToSent;
    }

    /**
     * Setter for property copyToSent.
     *
     * @param copyToSent New value of property copyToSent.
     */
    public void setCopyToSent(final boolean copyToSent) {
        this.copyToSent = copyToSent;
    }

    /**
     * Getter for property action.
     *
     * @return Value of property action.
     */
    public String getAction() {
        return this.action;
    }

    /**
     * Setter for property action.
     *
     * @param action New value of property action.
     */
    public void setAction(final String action) {
        this.action = action;
    }

    /**
     * Getter for property composeKey.
     *
     * @return Value of property composeKey.
     */
    public String getComposeKey() {
        return this.composeKey;
    }

    /**
     * Setter for property composeKey.
     *
     * @param composeKey New value of property composeKey.
     */
    public void setComposeKey(final String composeKey) {
        this.composeKey = composeKey;
    }
}
