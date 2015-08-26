<%@page contentType="text/html" import="org.apache.struts.Globals"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<p>
<font class="error"><bean:message key="errorUncaught.introduction"/></font>
<blockquote>
  <pre>
<%
    request.setAttribute("exception", request.getAttribute(Globals.EXCEPTION_KEY));
%>
      <c:out value="${'exception'}"/>
  </pre>
</blockquote>
</p>

<p>
   <bean:message key="errorBasic.returnTo"/>
   <html:link forward="inbox">INBOX</html:link>
</p>
<p>
   <html:link forward="logout"><bean:message key="errorBasic.logout"/></html:link>
</p>
<p>
   <bean:message key="errorBasic.instructions"/>
</p>