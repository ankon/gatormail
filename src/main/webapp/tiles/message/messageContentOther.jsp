<%@page contentType="text/html" import="javax.mail.Folder,
                                        javax.mail.Message,
                                        javax.mail.BodyPart,
                                        javax.mail.Part,
                                        java.util.Map,
                                        java.util.HashMap,
                                        com.sun.mail.imap.IMAPFolder,
                                        javax.mail.internet.MimeUtility"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%
    try {
        Folder folder = (Folder)request.getAttribute("realFolder");
        Message message = (Message)request.getAttribute("realMessage");
        String part = (String)request.getAttribute("part");
        // This is often not a BodyPart, we need to get the type and figure out
        // how to present it.
        Object o = request.getAttribute("body");
        BodyPart body = null;
        if (o instanceof BodyPart) {
            body = (BodyPart)o;
        } else {
            // TODO: Figure out how to deal with this case of where the
            // attachment *is* the content of the message
%>
<table class="messageBody" cellspacing="0" cellpadding="2" width="100%" border="0">
<tr>
 <th class="leftNavBar" width="100">FIXME!</th>
 <td class="messageContentOther">
  <i class="messageUnhandledAttachment">This message has an attachment that this
  webmail cannot deal with. You will need to use a different mail client to
  access this attachment. We are sorry for the inconvience, this is a known
  problem and we are trying to resolve this issue.</i>
 </td>
</table>
<%
            return;
        }


        // XXX This causes a lot of attachments to not show up and users complain
        if (!(body.getDisposition() == null || body.getDisposition().equalsIgnoreCase(Part.ATTACHMENT))) {
            // Don't show this part at all, it's not an attachment.
            //return;
        }

        out.println("<!-- messageContentOther: " + body.getContentType() + "-->");

        Map attachmentParams = new HashMap();
        attachmentParams.put("folder", folder.getFullName());
        if (folder instanceof IMAPFolder) {
            IMAPFolder imapFolder = (IMAPFolder)folder;
            attachmentParams.put("uid", new Long(imapFolder.getUID(message)));
        } else {
            attachmentParams.put("messageNumber", new Integer(message.getMessageNumber()));
        }

        attachmentParams.put("part", part);
        pageContext.setAttribute("attachmentParams", attachmentParams);
%>
<table class="messageBody" cellspacing="0" cellpadding="2" width="100%" border="0">
<tr>
 <th class="leftNavBar" width="100"><bean:message key="message.attachment.binary"/>:</th>
 <td class="messageContentOther">

<html:link forward="attachment" name="attachmentParams">
 <c:choose>
  <c:when test="${!empty body.fileName}">
<%
    // decode the word if needed 
    String fileName = body.getFileName();
    if (fileName != null && fileName.startsWith("=?")) {
        fileName = MimeUtility.decodeWord(fileName);
    }
    pageContext.setAttribute("fileName", fileName);

%>
  <bean:write name="fileName" filter="true"/>
  </c:when>
  <c:otherwise>
   Part <bean:write name="part" filter="true" />
  </c:otherwise>
 </c:choose>
</html:link>

<br/>
<bean:write name="body" property="contentType" filter="true" />
<br/>
<i class="attachmentWarningMessage"><bean:message key="message.attachment.binary.dangerous"/></i>
 </td>
</tr>
</table>
<%
    } catch (Throwable e) {
        e.printStackTrace(System.err);
        throw e;
    }
%>