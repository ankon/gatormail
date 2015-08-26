<%@ page import="edu.ufl.osg.webmail.prefs.PreferencesProvider,
                 edu.ufl.osg.webmail.Constants,
                 java.util.Properties,
                 edu.ufl.osg.webmail.User,
                 edu.ufl.osg.webmail.util.Util"%>
<%@page contentType="text/html"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<table style="background-color: #e6e6e6;" width="100%" cellspacing="0">
<%
    int currentAnnouncementId = 1;
    final User user = Util.getUser(session);
    PreferencesProvider pp = (PreferencesProvider)application.getAttribute(Constants.PREFERENCES_PROVIDER);
    Properties prefs = pp.getPreferences(user, session);
    int announcementId = Integer.parseInt(prefs.getProperty("announcement.last.seen","0"));
    prefs.setProperty("announcement.last.seen", Integer.toString(currentAnnouncementId));
    if (announcementId < currentAnnouncementId) {
%>
 <tr>
  <td class="announcement">
   <html:img page="/note.png" width="24" height="24" border="0"/>
   Check your
   <html:link forward="preferences" titleKey="link.preferences.title"><bean:message key="link.preferences"/></html:link>
   to take advantage of the new server side junk email filter which can automatically put email likely to be junk into
   a Junk folder. If you use this feature <b>do not</b> forget to check and empty your Junk folder regularly else your
   mailbox will fill up and you won't be able to receive email.
  </td>
 </tr>
<%
    }
    if (session.getAttribute("vacationEnabled") != null) {
%>
    <tr>
     <td class="alert">
      <html:img page="/note.png" width="24" height="24" border="0"/>
         You have a GatorLink vacation auto responder set.
         Do not forget to disable this in your
         <html:link forward="preferences" titleKey="link.preferences.title"><bean:message key="link.preferences"/></html:link>
         when you are no longer on vacation.
     </td>
    </tr>
<%
    }
%>
 <tr>
  <td>
<html:errors/>
<c:if test="${!empty result}">
<p align="center">
  <font class="result">
    <bean:write name="result" property="message" filter="true" scope="request"/>
  </font>
</p>
</c:if>
  </td>
 </tr>
</table>