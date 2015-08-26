<%@ page import="javax.mail.Folder"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="http://www.opensymphony.com/oscache" prefix="oscache"%>
<%
    final Folder folder = (Folder)request.getAttribute("folder");
%>
<table width="100%" border="0" cellpadding="4" cellspacing="0" class="folderMessageList">
 <tr class="darkBlueRow">
  <th>
<oscache:cache key="<%= "messageListHeader.jsp#" + folder.getFullName() %>" scope="session" time="300" groups="mailStore">
   <bean:message key="folder.view.folder.info" arg0="<%= folder.getFullName() %>" arg1="<%= String.valueOf(folder.getMessageCount()) %>" arg2="<%= String.valueOf(folder.getUnreadMessageCount()) %>" arg3="<%= String.valueOf(folder.getNewMessageCount()) %>" />
</oscache:cache>
  </th>
 </tr>
</table>