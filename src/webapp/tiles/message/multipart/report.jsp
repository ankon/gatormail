<%@page contentType="text/html" import="javax.mail.internet.MimeMultipart,
                                        javax.mail.BodyPart,
                                        edu.ufl.osg.webmail.util.Util,
                                        javax.mail.internet.MimeBodyPart"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%-- From: http://www.rfc-editor.org/rfc/rfc1892.txt
   The Multipart/Report content-type contains either two or three sub-
   parts, in the following order:

   1) [Required] The first body part contains human readable message.
   The purpose of this message is to provide an easily understood
   description of the condition(s) that caused the report to be
   generated, for a human reader who may not have a user agent capable
   of interpreting the second section of the Multipart/Report.

   The text in the first section may be in any MIME standards-track
   content-type, charset, or language.  Where a description of the error
   is desired in several languages or several media, a
   Multipart/Alternative construct may be used.

   This body part may also be used to send detailed information that
   cannot be easily formatted into a Message/Report body part.

   (2) [Required] A machine parsable body part containing an account of
   the reported message handling event. The purpose of this body part is
   to provide a machine-readable description of the condition(s) that
   caused the report to be generated, along with details not present in
   the first body part that may be useful to human experts.  An initial
   body part, Message/delivery-status is defined in [DSN].

   (3) [Optional] A body part containing the returned message or a
   portion thereof.  This information may be useful to aid human experts
   in diagnosing problems.  (Although it may also be useful to allow the
   sender to identify the message which the report was issued, it is
   hoped that the envelope-id and original-recipient-address returned in
   the Message/Report body part will replace the traditional use of the
   returned content for this purpose.)

   Return of content may be wasteful of network bandwidth and a variety
   of implementation strategies can be used.  Generally the sender
   should choose the appropriate strategy and inform the recipient of
   the required level of returned content required.  In the absence of
   an explicit request for level of return of content such as that
   provided in [DRPT], the agent that generated the delivery service
   report should return the full message content.

   When 8-bit or binary data not encoded in a 7 bit form is to be
   returned, and the return path is not guaranteed to be 8-bit or binary
   capable, two options are available.  The original message MAY be re-
   encoded into a legal 7-bit MIME message or the Text/RFC822-Headers
   content-type MAY be used to return only the original message headers.
--%>
<%
    out.println("<!-- Start message.multipart/report -->");

    Object o = request.getAttribute("body");
    MimeMultipart body = null;
    MimeBodyPart mimeBodyPart = null;
    String tileDefinition = null;
    int REPORT_PART = -1;

    if (o instanceof BodyPart) {
        BodyPart bodyPart = (BodyPart)o;
        body = (MimeMultipart)bodyPart.getContent();
    } else {
        body = (MimeMultipart)o;
    }

    String part = (String)request.getAttribute("part");

    // Part 0: Human readable message
    REPORT_PART = 0;
    mimeBodyPart = (MimeBodyPart)body.getBodyPart(REPORT_PART);
    tileDefinition = Util.getDefinitionName(mimeBodyPart.getContentType());
    request.setAttribute("body", mimeBodyPart);
    if (part == null) {
        request.setAttribute("part", Integer.toString(REPORT_PART));
    } else {
        request.setAttribute("part", part + "." + REPORT_PART);
    }
%><tiles:insert name="<%= tileDefinition %>"/><%


    // Part 2: returned message (optional)
    if (body.getCount() >= 3) {
        REPORT_PART = 2;
        mimeBodyPart = (MimeBodyPart)body.getBodyPart(REPORT_PART);
        tileDefinition = Util.getDefinitionName(mimeBodyPart.getContentType());
        request.setAttribute("body", mimeBodyPart);
        if (part == null) {
            request.setAttribute("part", Integer.toString(REPORT_PART));
        } else {
            request.setAttribute("part", part + "." + REPORT_PART);
        }
%><tiles:insert name="<%= tileDefinition %>"/><%
    }

    // Part 1: delivery status
    REPORT_PART = 1;
    mimeBodyPart = (MimeBodyPart)body.getBodyPart(REPORT_PART);
    tileDefinition = Util.getDefinitionName(mimeBodyPart.getContentType());
    request.setAttribute("body", mimeBodyPart);
    if (part == null) {
        request.setAttribute("part", Integer.toString(REPORT_PART));
    } else {
        request.setAttribute("part", part + "." + REPORT_PART);
    }
%><tiles:insert name="<%= tileDefinition %>"/><%

    out.println("<!-- End message.multipart/report -->");
%>