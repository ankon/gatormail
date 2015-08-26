<%@page contentType="text/html" import="edu.ufl.osg.webmail.*,javax.mail.*,java.util.*,javax.mail.internet.*,
                                        edu.ufl.osg.webmail.util.Util"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%
    out.println("<!-- Start message.multipart/related -->");

    // TODO: This should be better abstracted out and support things like images.
    Object o = request.getAttribute("body");
    MimeMultipart multipart = null;

    if (o instanceof BodyPart) {
        MimeBodyPart bp = (MimeBodyPart)o;
        multipart = (MimeMultipart)bp.getContent();
    } else {
        multipart = (MimeMultipart)o;
    }

    int partNumber = 0;
    String type = multipart.getContentType();
    if (type.toLowerCase().indexOf("start=\"") != -1) {
        // XXX This has not been tested
        String typeLower = type.toLowerCase();
        type = type.substring(typeLower.indexOf("start=\"") + "start=\"".length());
        type = type.substring(0, type.indexOf('"'));
        for (partNumber = multipart.getCount(); partNumber >= 0; partNumber--) {
            if (type.equals(((MimeBodyPart)multipart.getBodyPart(partNumber)).getContentID())) {
                break;
            }
        }
    }

    String part = (String)request.getAttribute("part");


    MimeBodyPart bodyPart = (MimeBodyPart)multipart.getBodyPart(partNumber);

    request.setAttribute("part", part + "." + partNumber);

    request.setAttribute("body", bodyPart);

    String tileDefinition = Util.getDefinitionName(bodyPart.getContentType());

%><tiles:insert name="<%= tileDefinition %>"/><%

    out.println("<!-- End message.multipart/related -->");
%>