<%@page contentType="text/html" import="edu.ufl.osg.webmail.Constants"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/webmail" prefix="wm"%>

<bean:define id="messageParams" name="messageParams" scope="request" type="java.util.Map"/>
<bean:define id="messageFolder" name="messageFolder" scope="request" type="javax.mail.Folder"/>

<table width="100%" cellspacing="0" class="messageNavBar">
  <tr>
    <%-- Prev/Up/Next Links --%>
    <th width="15%">
      <nobr>
       <html:link forward="prevMessage" name="messageParams" scope="request" titleKey="link.previous.title" style="color:#ffffff;"><bean:message key="link.previous"/></html:link>
       <%
         pageContext.setAttribute("folderName", messageFolder.getFullName());
         pageContext.setAttribute("sort", (String)request.getAttribute("folderSort"));
       %>
       &nbsp;
       <a href="folder.do?folder=<c:out value="${folderName}"/>&amp;sort=<c:out value="${folderSort}"/>" title="Back to Folder" style="color:#ffffff;"><bean:message key="link.up"/></a>
       &nbsp;
       <html:link forward="nextMessage" name="messageParams" scope="request" titleKey="link.next.title" style="color:#ffffff;"><bean:message key="link.next"/></html:link>
      </nobr>
    </th>

    <%-- Delete Link --%>
    <th width="20%">
      <c:choose>
       <c:when test="${folder.fullName != 'INBOX.Trash' and folder.fullName != 'INBOX/Trash'}">
        <html:link forward="deleteMessage" name="messageParams" scope="request" titleKey="link.delete.title" style="color:#ffffff;"><bean:message key="link.delete"/></html:link>
       </c:when>
       <c:otherwise>
        <%
            messageParams.put(Constants.DELETE_FOREVER, "true");
        %>
        <html:link forward="deleteMessage" name="messageParams" scope="request" titleKey="link.deleteForever.title" style="color:#ffffff;"><bean:message key="link.deleteForever"/></html:link>
        <%
            messageParams.remove(Constants.DELETE_FOREVER);
        %>
       </c:otherwise>
      </c:choose>
    </th>

    <%-- Reply Link --%>
    <th width="20%">
      <html:link forward="reply" name="messageParams" scope="request" titleKey="link.reply.title" style="color:#ffffff;">
        <bean:message key="link.reply"/>
      </html:link>
      <%
        messageParams.put("action", "reply-all");
      %>
      &nbsp;
      <html:link forward="reply" name="messageParams" scope="request" titleKey="link.reply-all.title" style="color:#ffffff;">
        <bean:message key="link.reply-all"/>
      </html:link>
      <%
        messageParams.remove("action");
      %>
    </th>

    <%-- Forward Link --%>
    <th width="20%">
      <html:link forward="forward" name="messageParams" scope="request" titleKey="link.forward.title" style="color:#ffffff;">
        <bean:message key="link.forward"/>
      </html:link>
    </th>

  </tr>
</table>
