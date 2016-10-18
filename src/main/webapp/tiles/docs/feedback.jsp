<%@page contentType="text/html" import="org.apache.struts.action.ActionErrors"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>

<h1><bean:message key="feedback.title"/></h1>

<p>
  If you are having trouble using GatorMail, you might find an answer
  to the problem on our <html:link forward="help">help
  page</html:link>. If you need further assistance, or want to send feedback
  for the GatorMail application please use the issue tracker
  on <html:link href="https://github.com/ankon/gatormail/issues">the project page.</html:link>.
</p>
