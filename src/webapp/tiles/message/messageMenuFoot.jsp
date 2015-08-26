<%@page contentType="text/html"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>

<bean:define id="messageParams" name="messageParams" scope="request" type="java.util.Map"/>
<bean:define id="messageFolder" name="messageFolder" scope="request" type="javax.mail.Folder"/>

<table class="messageFooter" cellspacing="0" cellpadding="2" width="100%" border="0">
 <tr>
   <th class="leftNavBar" width="100">&nbsp;</th>
   <th class="darkBlueRow" width="50%">
   <%
     if (messageFolder != null) {
   %>
        <tiles:insert page="/tiles/message/messageMenu.jsp" flush="true"/>
   <%
     }
   %>
   </th>

   <th align="right" class="darkBlueRow">
    <table width="100%" cellspacing="0" class="messageNavBar">
     <tr>
      <th width="15%">
       <nobr>
        <%-- Priner friendly Link --%>
        <html:link forward="printerFriendly" name="messageParams" scope="request" titleKey="link.printerFriendly.title" style="color:#ffffff;">
         <bean:message key="link.printerFriendly"/>
        </html:link>
       </nobr>
      </th>
     </tr>
    </table>
   </th>

   <th align="right" class="darkBlueRow">
    <table width="100%" cellspacing="0" class="messageNavBar">
     <tr>
      <th width="15%">
       <nobr>
        <html:link forward="rawMessage" name="messageParams" scope="request" titleKey="link.message.source.title" style="color:#ffffff;"><bean:message key="link.message.source"/></html:link>
       </nobr>
      </th>
     </tr>
    </table>
   </th>
 </tr>
</table>
