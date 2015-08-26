<%@page contentType="text/html; charset=utf-8"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<tiles:insert definition="errorLayout">
 <tiles:put name="title">
  <bean:message key="errorBasic.title"/>
 </tiles:put>
 <tiles:put name="body" value="/tiles/error/errorUncaughtMessage.jsp"/>
</tiles:insert>
