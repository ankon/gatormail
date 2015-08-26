<%@page contentType="text/html; charset=utf-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/struts-logic" prefix="logic"%>
<%
if (request.getAttribute("messages") == null) {
    %>
    <logic:forward name="inbox"/>
    <%
}
%>
<tiles:insert definition="folderLayout">
  <tiles:put name="title">
    <bean:message key="folder.title"/>
  </tiles:put>
  <tiles:put name="folderName"><%= request.getParameter("folder") %></tiles:put>
</tiles:insert>
