<%@page contentType="text/html; charset=utf-8"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<tiles:insert definition="simpleLayout">
 <tiles:put name="title">
  <bean:message key="feedback.title"/>
 </tiles:put>
 <tiles:put name="body" value="/tiles/docs/feedback.jsp"/>
</tiles:insert>
