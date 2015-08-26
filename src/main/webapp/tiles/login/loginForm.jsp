<%@page contentType="text/html" import="org.apache.struts.action.ActionErrors"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%
String loginFocus = "username";
%>
<c:if test="${! empty loginForm}">
 <c:if test="${! empty loginForm.username}">
<%
  loginFocus = "password";
%>
 </c:if>
</c:if>
<html:errors property="<%= ActionErrors.GLOBAL_ERROR %>"/>
<html:form action="login" focus="<%= loginFocus %>">
<table class="loginForm">
 <tr>
  <td class="usernamePrompt"><bean:message key="prompt.username"/></td>
  <td class="usernameInput"><html:text property="username" size="16"/></td>
  <td class="usernameErrors"><html:errors property="username"/></td>
 </tr>
 <tr>
  <td class="passwordPrompt"><bean:message key="prompt.password"/></td>
  <td class="passwordInput"><html:password property="password" size="16" redisplay="false"/></td>
  <td class="passwordErrors"><html:errors property="password"/></td>
 </tr>
 <tr>
  <td class="submitButton"><html:submit property="action" styleClass="button"><bean:message key="button.login"/></html:submit></td>
  <td class="resetButton"><html:reset property="action" styleClass="button"><bean:message key="button.reset"/></html:reset></td>
  <td><!-- empty --></td>
 </tr>
</table>
</html:form>
