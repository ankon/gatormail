<%@page contentType="text/html" import="org.apache.struts.action.ActionErrors"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>

<p>
<font class="error"><bean:message key="errorBasic.introduction"/></font>
<blockquote>
  <html:errors property="<%= ActionErrors.GLOBAL_ERROR %>"/>
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
