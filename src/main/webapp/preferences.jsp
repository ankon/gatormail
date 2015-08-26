<%@page contentType="text/html; charset=utf-8"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<tiles:insert definition="defaultLayout">
 <tiles:put name="title">
  <bean:message key="preferences.title"/>
 </tiles:put>
 <tiles:put name="body" value="/tiles/preferences/preferences.jsp"/>
</tiles:insert>
