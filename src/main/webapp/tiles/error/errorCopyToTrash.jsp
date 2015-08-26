<%@page contentType="text/html" import="edu.ufl.osg.webmail.Constants,
                                        org.apache.struts.action.ActionErrors,
                                        edu.ufl.osg.webmail.util.Util,
                                        edu.ufl.osg.webmail.wrappers.MessageWrapper,
                                        java.util.Map,
                                        java.util.HashMap,
                                        javax.mail.Folder,
                                        edu.ufl.osg.webmail.util.Util,
                                        javax.mail.Message"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/webmail" prefix="wm"%>

<p>
<bean:message key="errorCopy.introduction" arg0="<%=Constants.TRASH_FOLDER%>"/>
<blockquote>
   <html:errors property="<%= ActionErrors.GLOBAL_ERROR %>"/>
</blockquote>
</p>

<p>
   <bean:message key="errorCopy.overQuota.message"/>
   <bean:message key="errorCopyToTrash.overQuota.instructions" arg0="<%=Constants.TRASH_FOLDER%>"/>
</p>

<p>
<blockquote>
   <table class="headerTable" cellpadding="3">
     <tr class="subheader">
       <td><bean:message key="label.subject"/></td>
       <td><bean:message key="label.size"/></td>
       <td><bean:message key="label.deleteForever"/></td>
     </tr>
<bean:define id="deleteAction" name="<%=Constants.DELETE_ACTION%>"
             type="java.lang.String" scope="request"/>
<c:forEach items="${messageList}" var="message">
<%
    Message message = (Message)pageContext.getAttribute("message");
    Folder folder = Util.getFolder(message.getFolder()); // ensure folder is opened
    MessageWrapper wrappedMessage = new MessageWrapper(message);
    pageContext.setAttribute("wrappedMessage", wrappedMessage);

    Map deleteParams = new HashMap();
    Util.addMessageParams(message, deleteParams);
    deleteParams.put(Constants.DELETE_FOREVER, "true");
    pageContext.setAttribute("deleteParams", deleteParams);
%>
     <tr>
       <td><bean:write name="wrappedMessage" property="subject" filter="true"/></td>
       <td><wm:write name="wrappedMessage" property="size" filter="false"
	   formatKey="general.size.format" formatClassKey="general.size.formatter"/></td>
       <td>
	   <html:link forward="<%=deleteAction%>" name="deleteParams"><bean:message key="label.deleteForever"/></html:link>
       </td>
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
