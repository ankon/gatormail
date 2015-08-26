<%@page contentType="text/html" import="edu.ufl.osg.webmail.Constants,
                                        org.apache.struts.action.ActionErrors"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>

<p>
    <font class="result"><bean:message key="send.result.success"/></font>
</p>

<p>
<bean:message key="errorCopy.introduction" arg0="<%=Constants.SENT_FOLDER%>"/>
<blockquote>
   <html:errors property="<%= ActionErrors.GLOBAL_ERROR %>"/>
</blockquote>
</p>

<p>
   <bean:message key="errorCopy.overQuota.message"/>
   <bean:message key="errorCopy.overQuota.instructions" arg0="<%=Constants.TRASH_FOLDER%>"/>
</p>

<p>
   <bean:message key="errorBasic.returnTo"/>
   <html:link forward="inbox">INBOX</html:link>
</p>