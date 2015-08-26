<%@page contentType="text/html" import="edu.ufl.osg.webmail.util.Util"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>

<bean:define id="folder" name="folder" type="javax.mail.Folder" scope="request"/>

<table class="headerTable" cellpadding="0" cellspacing="0" width="100%">
  <tr class="header">
    <th>
	<strong><bean:message key="folderManageModify.title"/>:</strong>
	<bean:write name="folder" property="fullName" filter="true"/>
    </th>
  </tr>

  <%-- create new subfolder --%>
  <tr>
    <td>
      <html:form action="createFolder">
      <html:hidden property="folder" value="<%=folder.getFullName()%>"/>
      <table class="headerTable" cellpadding="3" cellspacing="0" width="100%">
	<tr class="subheader">
	  <td>
	      <bean:message key="folderManageModify.subFolder.header"/>
	  </td>
	</tr>
	<tr>
	  <td>
	      <bean:message key="folderManageModify.subFolder.desc"/>
	      <bean:write name="folder" property="fullName" filter="true"/>:
	  </td>
	</tr>
	<tr>
	  <td>
	      <html:text property="newFolder" maxlength="30"/>
	      <html:submit styleClass="button">
	        <bean:message key="button.folderManageModify.newFolder"/>
	      </html:submit>
	  </td>
	</tr>
      </table>
      </html:form>
    </td>
  </tr>
  <tr><td>&nbsp;</td></tr>

<%-- Rename Folder --%>
<%
    if (!Util.isReservedFolder(folder.getFullName(), session)) {
%>
  <tr>
    <td>
	<html:form action="renameFolder">
	<html:hidden property="folder" value="<%=folder.getFullName()%>"/>
	<table class="headerTable" cellpadding="3" cellspacing="0" width="100%">
	  <tr class="subheader">
	    <td colspan="2">
	      <bean:message key="folderManageModify.renameFolder.header"/>
	    </td>
	  </tr>
	  <tr>
	    <td align="right" width="15%">
	        <bean:message key="folderManageModify.renameFolder.name"/>:
	    </td>
	    <td>
		<bean:write name="folder" property="name" filter="true"/>
	    </td>
	  </tr>
	  <tr>
	    <td align="right" width="15%">
	      <bean:message key="folderManageModify.renameFolder.renameTo"/>:
	    </td>
	    <td>
		<html:text property="newFolder" maxlength="30"/>
		<html:submit styleClass="button">
		  <bean:message key="button.folderManageModify.renameFolder"/>
		</html:submit>
	    </td>
	  </tr>
        </table>
	</html:form>
    </td>
  </tr>
  <tr><td>&nbsp;</td></tr>
<%
  }
%>

<%-- don't let user unsubscribe the INBOX! --%>
<c:if test="${folder.fullName != 'INBOX'}">
  <tr>
    <td>
	<html:form action="changeSubscribed">
	<html:hidden property="folder" value="<%=folder.getFullName()%>"/>
	<table class="headerTable" cellpadding="3" cellspacing="0" width="100%">
	  <tr class="subheader">
	    <td>
		<bean:message key="folderManageModify.subscription.header"/>
	    </td>
	  </tr>
	  <tr>
	    <td>
		<bean:message key="folderManageModify.subscription.status"/>:
        <c:choose>
         <c:when test="${isSubscribed}">
          <bean:message key="folderManageModify.subscription.subscribed"/>
         </c:when>
         <c:otherwise>
          <bean:message key="folderManageModify.subscription.unsubscribed"/>
         </c:otherwise>
        </c:choose>
	    </td>
	  </tr>
	  <tr>
	    <td>
        <c:choose>
         <c:when test="${isSubscribed}">
		  <html:submit styleClass="button" property="action">
		    <bean:message key="button.unsubscribeFolder"/>
		  </html:submit>
         </c:when>
         <c:otherwise>
		  <html:submit styleClass="button" property="action">
		    <bean:message key="button.subscribeFolder"/>
		  </html:submit>
         </c:otherwise>
        </c:choose>
	    </td>
	  </tr>
     </table>
	</html:form>
    </td>
  </tr>
  <tr><td>&nbsp;</td></tr>
</c:if>

  <tr>
    <td>
	<p>
	  <html:link forward="folderManage"><bean:message key="link.folderManage.back"/></html:link>
	</p>
    </td>
  </tr>

</table>
