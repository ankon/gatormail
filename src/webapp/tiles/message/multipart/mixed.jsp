<%@page contentType="text/html" import="edu.ufl.osg.webmail.*,javax.mail.*,java.util.*,javax.mail.internet.*,
                                        edu.ufl.osg.webmail.util.Util,
                                        javax.activation.DataHandler,
                                        javax.activation.CommandMap,
                                        java.io.StringBufferInputStream,
                                        javax.activation.MailcapCommandMap"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%--
The primary subtype for multipart, "mixed", is intended for use when the body
parts are independent and need to be bundled in a particular order. Any
multipart subtypes that an implementation does not recognize must be treated as
being of subtype "mixed".
<th class="leftNavBar" width="100">&nbsp;</th>
<td class="messageMultipartMixed">
--%>
<%
    out.println("<!-- Start message.multipart/mixed -->");

    Object o = request.getAttribute("body");
    MimeMultipart body = null;
    if (o instanceof BodyPart) {
        BodyPart bodyPart = (BodyPart)o;
        body = (MimeMultipart)bodyPart.getContent();
    } else {
        body = (MimeMultipart)o;
    }
    String part = (String)request.getAttribute("part");

    for (int i = 0; i < body.getCount(); i++) {
        MimeBodyPart mimeBodyPart = (MimeBodyPart)body.getBodyPart(i);
        //JAFUtil.addCommmandMapTypes(bodyPart);

        String tileDefinition = Util.getDefinitionName(mimeBodyPart.getContentType());
        request.setAttribute("body", mimeBodyPart);
        if (part == null) {
            request.setAttribute("part", Integer.toString(i));
        } else {
            request.setAttribute("part", part + "." + i);
        }

%><tiles:insert name="<%= tileDefinition %>"/><%
    }

    out.println("<!-- End message.multipart/mixed -->");
%>