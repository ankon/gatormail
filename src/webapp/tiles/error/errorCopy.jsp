<%@page contentType="text/html" import="javax.mail.Folder,
                                        org.apache.struts.action.ActionErrors,
                                        edu.ufl.osg.webmail.util.Util,
                                        edu.ufl.osg.webmail.Constants,
                                        edu.ufl.osg.webmail.wrappers.MessageWrapper,
                                        javax.mail.Message"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/webmail" prefix="wm"%>

<bean:define id="folderName" name="folderName" type="java.lang.String" scope="request"/>
<p>
<bean:message key="errorCopy.introduction" arg0="<%=folderName%>"/>
<blockquote>
   <html:errors property="<%= ActionErrors.GLOBAL_ERROR %>"/>
</blockquote>
</p>

<p>
   <bean:message key="errorCopy.overQuota.message"/>
   <bean:message key="errorCopy.overQuota.instructions" arg0="<%=Constants.TRASH_FOLDER%>"/>
</p>

<p>
<bean:message key="errorCopy.table.introduction"/>
<blockquote>
   <table class="headerTable" cellpadding="3">
     <tr class="subheader">
       <td><bean:message key="label.subject"/></td>
       <td><bean:message key="label.size"/></td>
     </tr>

<c:forEach items="${messageList}" var="message">
<%
    Message message = (Message)pageContext.getAttribute("message");
    Folder folder = Util.getFolder(message.getFolder()); // ensure folder is opened
    MessageWrapper wrappedMessage = new MessageWrapper(message);
    pageContext.setAttribute("wrappedMessage", wrappedMessage);
%>
     <tr>
       <td><bean:write name="wrappedMessage" property="subject" filter="true"/></td>
       <td><wm:write name="wrappedMessage" property="size" filter="false"
	   formatKey="general.size.format" formatClassKey="general.size.formatter"/></td>
     </tr>
<%
    Util.releaseFolder(folder); // close folder
%>
</c:forEach>
   </table>
</blockquote>
</p>

<p>
   <bean:message key="errorBasic.returnTo"/>
   <html:link forward="inbox">INBOX</html:link>
</p>
