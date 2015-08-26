<%@ page import="javax.mail.Message,
                 javax.mail.Folder,
                 java.util.Map,
                 java.util.HashMap,
                 edu.ufl.osg.webmail.util.Util"%>
<%@page contentType="text/html"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<html:html locale="true" xhtml="true">
<head>
 <title>
  <bean:message key="site.title"/>
  <bean:message key="message.title"/>
 </title>
 <link rel="shortcut icon" href="favicon.ico" type="image/x-icon" />
 <link rel="start" href="<html:rewrite forward="INBOX"/>" title="INBOX"/>
 <link rel="help" href="<html:rewrite forward="help"/>" title="GatorMail Help"/>
 <html:base/>
 <link rel="stylesheet" type="text/css" href="<html:rewrite forward="CSS"/>"/>
</head>
<body>
 <div class="body">
  <tiles:get name="body"/>
 </div>
</body>
</html:html>