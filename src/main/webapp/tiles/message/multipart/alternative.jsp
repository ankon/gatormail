<%@page contentType="text/html" import="edu.ufl.osg.webmail.util.Util,
                                        javax.mail.internet.MimeMultipart,
                                        javax.mail.BodyPart,
                                        javax.mail.internet.MimeBodyPart"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%--
The multipart/alternative type is syntactically identical to multipart/mixed,
but the semantics are different. In particular, each of the parts is an
"alternative" version of the same information.

Systems should recognize that the content of the various parts are
interchangeable. Systems should choose the "best" type based on the local
environment and preferences, in some cases even through user interaction. As
with multipart/mixed, the order of body parts is significant. In this case, the
alternatives appear in an order of increasing faithfulness to the original
content. In general, the best choice is the LAST part of a type supported by the
recipient system's local environment.
--%>
<%
    Object o = request.getAttribute("body");
    MimeMultipart multipart = null;

    if (o instanceof BodyPart) {
        MimeBodyPart bp = (MimeBodyPart)o;
        multipart = (MimeMultipart)bp.getContent();
    } else {
        multipart = (MimeMultipart)o;
    }



    String part = (String)request.getAttribute("part");

    out.println("<!-- Start message.multipart/alternative -->");

    for (int i = multipart.getCount() - 1; i >= 0 ; i--) {
        MimeBodyPart bodyPart = (MimeBodyPart)multipart.getBodyPart(i);

        if (part != null) {
            request.setAttribute("part", part + "." + i);
        } else {
            request.setAttribute("part", Integer.toString(i));
        }

        String tileDefinition = Util.getDefinitionName(bodyPart.getContentType());
        if (Util.isValidDefinition(tileDefinition, request, application)) {
            request.setAttribute("body", bodyPart);
            %><tiles:insert name="<%= tileDefinition %>"/><%
            break;
        }
    }

    out.println("<!-- End message.multipart/alternative -->");
%>