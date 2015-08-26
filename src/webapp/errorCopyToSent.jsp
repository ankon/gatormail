<%@page contentType="text/html; charset=utf-8"
        import="edu.ufl.osg.webmail.Constants"
%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<tiles:insert definition="errorLayout">
 <tiles:put name="title">
  <bean:message key="errorCopy.title.reserved" arg0="<%=Constants.SENT_FOLDER%>"/>
 </tiles:put>
 <tiles:put name="body" value="/tiles/error/errorCopyToSent.jsp"/>
</tiles:insert>
