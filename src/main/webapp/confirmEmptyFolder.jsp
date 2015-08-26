<%@page contentType="text/html; charset=utf-8"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<tiles:insert definition="folderManageLayout">
  <tiles:put name="title">
    <bean:message key="confirmEmptyFolder.title"/>
  </tiles:put>
  <tiles:put name="body" value="/tiles/folder/confirmEmptyFolder.jsp"/>
</tiles:insert>