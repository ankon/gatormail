<%@page contentType="text/html" import="javax.mail.Folder,
                                        javax.mail.Message,
                                        javax.mail.BodyPart,
                                        java.util.Map,
                                        java.util.HashMap,
                                        com.sun.mail.imap.IMAPFolder"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%
    try {
        // Get this part.
        Folder folder = (Folder)request.getAttribute("realFolder");
        Message message = (Message)request.getAttribute("realMessage");
        String part = (String)request.getAttribute("part");

        Map imageParams = new HashMap();
        imageParams.put("folder", folder);
        if (folder instanceof IMAPFolder) {
            IMAPFolder imapFolder = (IMAPFolder)folder;
            imageParams.put("uid", new Long(imapFolder.getUID(message)));
        } else {
            imageParams.put("messageNumber", new Integer(message.getMessageNumber()));
        }

        imageParams.put("part", part);
        pageContext.setAttribute("imageParams", imageParams);
%>
<table class="messageBody" cellspacing="0" cellpadding="2" width="100%" border="0">
<tr>
 <th class="leftNavBar" width="100">&nbsp;</th>
 <td class="messageImageDefault">
<!--
<bean:message key="message.attachment.image"/>
-->
<html:link forward="attachment" name="imageParams">
 <html:img name="imageParams" pageKey="message.attachment.url" alt="an image"/>
</html:link>
 </td>
</table>
<%
    } catch (Exception e) {
        e.printStackTrace(System.err);
        throw e;
    }
%>