<%@page contentType="text/html" import="javax.mail.Message,
                                        javax.mail.Folder,
                                        java.util.Map,
                                        java.util.HashMap,
                                        edu.ufl.osg.webmail.util.Util,
                                        edu.ufl.osg.webmail.User,
                                        edu.ufl.osg.webmail.prefs.PreferencesProvider,
                                        edu.ufl.osg.webmail.Constants,
                                        java.util.Properties,
                                        java.util.List,
                                        java.util.Arrays,
                                        java.util.Iterator"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/webmail" prefix="wm"%>
<%--
TODO: Make to and other email's need to be run through the formatter like from is.
The look of the message header needs to be reworked, probably should be in a tile.
--%>
<%
    Message message = (Message)request.getAttribute("message");

    Folder messageFolder = message.getFolder();
    request.setAttribute("messageFolder", messageFolder);
    String sort = (String)request.getAttribute("folderSort");


    if (messageFolder != null) {
        messageFolder = Util.getFolder(messageFolder);

        Map messageParams = new HashMap();
        Util.addMessageParams(message, sort, messageParams);
        request.setAttribute("messageParams", messageParams);
%>
 <table class="messageHeader" width="100%" cellspacing="0" cellpadding="2" border="0">
   <tr>
     <th class="leftNavBar" width="100">&nbsp;</th>

     <th class="darkBlueRow" width="50%">
       <tiles:insert page="/tiles/message/messageMenu.jsp" flush="true"/>
     </th>

     <th align="right" class="darkBlueRow">
    <table width="100%" cellspacing="0" class="messageNavBar">
     <tr>
      <th width="15%">
       <nobr>
<%
        final User user = Util.getUser(session);
        final PreferencesProvider pp = (PreferencesProvider)application.getAttribute(Constants.PREFERENCES_PROVIDER);
        final Properties prefs = pp.getPreferences(user, session);
        final int junkThreshold = Integer.valueOf(prefs.getProperty("message.junk.threshold", "8")).intValue();

        final String junkPattern;
        if (junkThreshold > 0) {
            junkPattern = "******************************************".substring(0, Math.min(30, junkThreshold));
        } else {
            junkPattern = "";
        }
        boolean isJunk = false;

        Folder folder = (Folder)request.getAttribute("folder");
        Util.getFolder(folder);
        String[] junkHeadersArray = message.getHeader("X-UFL-Spam-Level");
        Util.releaseFolder(folder);
        if (junkHeadersArray != null) {
            List junkHeadersList = Arrays.asList(junkHeadersArray);
            Iterator iter = junkHeadersList.iterator();
            while (!isJunk && iter.hasNext()) {
                String header = (String)iter.next();
                if (header != null && header.indexOf(junkPattern) != -1) {
                    isJunk = true;
                }
            }
        }

        if (!isJunk) {
%>
    <%-- Report Spam Link --%>
      <html:link forward="reportSpam" name="messageParams" scope="request" titleKey="link.reportSpam.title" style="color:#ffffff;">
        <bean:message key="link.report.spam"/>
      </html:link>
<%
        } else {
%>
    <%-- Report Ham Link --%>
      <html:link forward="reportHam" name="messageParams" scope="request" titleKey="link.reportHam.title" style="color:#ffffff;">
        <bean:message key="link.report.ham"/>
      </html:link>
<%
        }
%>
       </nobr>
      </th>
     </tr>
    </table>
     </th>

     <th align="right" class="darkBlueRow">
       <html:form method="post" action="modifyMessage">
<%
    messageParams = (Map)request.getAttribute("messageParams");
    if (messageParams.get("uid") != null) {
        String uid = messageParams.get("uid").toString();
%><html:hidden property="uid" value="<%=uid%>"/><%
    } else if (messageParams.get("messageNumber") != null) {
        String messageNumber = messageParams.get("messageNumber").toString();
%><html:hidden property="messageNumber" value="<%=messageNumber%>"/><%
    }
%>
     <html:hidden property="folder" value="<%=messageFolder.getFullName()%>"/>
     <tiles:insert page="/tiles/common/moveCopy.jsp" flush="true"/>
     </html:form>
     </th>
   </tr>
 </table>
<%
        Util.releaseFolder(messageFolder);
} // if (messageFolder != null)
%>
