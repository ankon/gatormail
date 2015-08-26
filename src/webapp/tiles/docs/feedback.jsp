<%@page contentType="text/html" import="org.apache.struts.action.ActionErrors"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>

<h1><bean:message key="feedback.title"/></h1>

<p>
  If you are having trouble using GatorMail, you might find an answer
  to the problem on our <html:link forward="help">help
  page</html:link>. If you need further assistance, you may contact the
  Help Desk at (352)392-HELP or
  <html:link forward="helpdeskFeedback">helpdesk@ufl.edu</html:link>.
</p>

<p>
  If you would like to send us feedback on the GatorMail application,
  such as bug reports, feature requests, or comments on your user
  experience, please <html:link forward="composeFeedback">proceed to
  the feedback form</html:link>. GatorMail was designed largely
  based on user feedback, and we definitely welcome your input!
</p>
