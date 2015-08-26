<%@page contentType="text/html"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<nobr>
<html:select property="toFolder">
   <html:option value=""><bean:message key="select.folder"/></html:option>
   <c:forEach items="${folderBeanList}" var="folderBean">
     <html:option value="${folderBean.fullName}"><bean:write name="folderBean" property="fullName"/></html:option>
   </c:forEach>
</html:select>
<html:submit property="action" styleClass="button"><bean:message key="button.moveToFolder"/></html:submit>
<html:submit property="action" styleClass="button"><bean:message key="button.copyToFolder"/></html:submit>
</nobr>
