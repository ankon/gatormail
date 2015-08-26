<%@page contentType="text/html" import="java.util.Map,
                                        java.util.HashMap,
                                        javax.mail.Folder"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/webmail" prefix="wm"%>

<p>
   <strong><bean:message key="confirmDeleteFolder.header"/>:</strong> <bean:write name="folder" property="fullName" filter="true"/>
</p>

<p>
<bean:message key="confirmDeleteFolder.listIntro"/>:
<br/>
<blockquote>
  <table class="headerTable" cellpadding="3" cellspacing="0">
    <tr class="subheader">
      <td><bean:message key="folderManage.table.folder"/></td>
      <td><bean:message key="folderManage.table.numMessages"/></td>
    </tr>
<c:forEach items="${affectedFolderList}" var="folder">
<%
    Folder folder = (Folder)pageContext.getAttribute("folder");
    Map folderParams = new HashMap();
    folderParams.put("folder", folder.getFullName());
    pageContext.setAttribute("folderParams", folderParams);
%>
    <tr>
      <td><html:link forward="folder" paramId="folder" paramName="folder" paramProperty="fullName">
	  <bean:write name="folder" property="fullName" filter="true"/></html:link>
      </td>
      <td align="right"><bean:write name="folder" property="messageCount"/></td>
    </tr>
</c:forEach>
  </table>
</blockquote>

<p><bean:message key="confirmDeleteFolder.prompt"/></p>

<bean:define id="folderName" name="folder" property="fullName" scope="request" type="java.lang.String"/>

<p>
   <html:form action="performDeleteFolder">
   <html:hidden property="folder" value="<%= folderName %>"/>
   <html:submit property="action" styleClass="button">
      <bean:message key="button.cancel"/>
   </html:submit>
   &nbsp;&nbsp;
   <html:submit property="action" styleClass="button">
      <bean:message key="button.ok"/>
   </html:submit>
   </html:form>
</p>