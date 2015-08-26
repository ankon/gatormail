<%@page contentType="text/html" import="javax.mail.Folder,
                                        javax.mail.Message,
                                        javax.mail.internet.MimeMessage,
                                        javax.mail.BodyPart,
                                        java.util.Map,
                                        java.util.HashMap,
                                        com.sun.mail.imap.IMAPFolder"%>
<%@taglib uri="/tags/struts-bean" prefix="bean" %>
<%@taglib uri="/tags/struts-html" prefix="html" %>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%
    try {
        Folder realFolder = (Folder)request.getAttribute("realFolder");
        Message realMessage = (Message)request.getAttribute("realMessage");

        Object o = request.getAttribute("body");
        String part = (String)request.getAttribute("part");

        MimeMessage message = null;
        if (o instanceof BodyPart) {
            BodyPart bodyPart = (BodyPart)o;
            message = (MimeMessage)bodyPart.getContent();
        } else {
            message = (MimeMessage)o;
        }
        request.setAttribute("message", message);

        // Download as attachment stuff.
        Map attachmentParams = new HashMap();
        attachmentParams.put("folder", realFolder.getFullName());
        if (realFolder instanceof IMAPFolder) {
            IMAPFolder imapFolder = (IMAPFolder)realFolder;
            attachmentParams.put("uid", new Long(imapFolder.getUID(realMessage)));
        } else {
            attachmentParams.put("messageNumber", new Integer(realMessage.getMessageNumber()));
        }

        attachmentParams.put("part", part);
        pageContext.setAttribute("attachmentParams", attachmentParams);
%>
<table class="messageBody" cellspacing="0" cellpadding="2" width="100%" border="0">
<tr>
 <th class="leftNavBar" width="100">&nbsp;</th>
 <td class="darkBlueRow" class="messageMessageRfc822"><bean:message key="message.attachment.message"/></td>
</tr>
</table>
<tiles:insert name="message.message"/>
<%
    } catch (Throwable t) {
        t.printStackTrace();
        throw t;
    }
%>