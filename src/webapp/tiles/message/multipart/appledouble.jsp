<%@page contentType="text/html" import="javax.mail.internet.MimeMultipart,
                                        javax.mail.BodyPart,
                                        javax.mail.internet.MimeBodyPart,
                                        edu.ufl.osg.webmail.util.Util"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%--
This is just like multipart/mixed but there will only be 2 parts and we are
supressing the application/applefile part.
--%>
<div class="messageMultipartAppledouble">
<%
    out.println("<!-- Start message.multipart/appledouble -->");

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

        // Skip parts of application/applefile type.
        if (!mimeBodyPart.isMimeType("application/applefile")) {
            String tileDefinition = Util.getDefinitionName(mimeBodyPart.getContentType());
            request.setAttribute("body", mimeBodyPart);
            if (part == null) {
                request.setAttribute("part", Integer.toString(i));
            } else {
                request.setAttribute("part", part + "." + i);
            }

            %><tiles:insert name="<%= tileDefinition %>"/><%
        }
    }

    out.println("<!-- End message.multipart/appledouble -->");
%>
</div>