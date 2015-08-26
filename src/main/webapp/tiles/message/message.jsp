<%@page contentType="text/html" import="javax.mail.Message,
                                        javax.mail.Address,
                                        edu.ufl.osg.webmail.wrappers.MessageWrapper,
                                        edu.ufl.osg.webmail.util.Util,
                                        javax.mail.Folder"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/webmail" prefix="wm"%>
<%
    Message message = (Message)request.getAttribute("message");
    Folder folder = message.getFolder();
    if (folder != null) {
        Util.getFolder(folder);
    }
    MessageWrapper wrappedMessage = new MessageWrapper(message);
    pageContext.setAttribute("wrappedMessage", wrappedMessage);
%>

<table class="messageHeader"  width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
   <th class="leftNavBar" width="100"><bean:message key="message.from"/>:</th>
   <td class="lightBlueRow">
<%-- TODO: Other things should be wrapped in notEmpty like this. This prevents iterators from complaining when there is nothing to iterate. --%>
     <c:if test="${! empty wrappedMessage.from}">
       <c:forEach items="${wrappedMessage.from}" var="from">
         <wm:write name="from" filter="false" formatKey="message.view.email.format" formatClassKey="message.email.formatter"/>
       </c:forEach>
       <%-- ADD link to selectAddresses if we are not in printerFriendly view --%>
       <c:if test="${! printerFriendly}">
         <html:link forward="selectAddresses" name="messageParams">
           <bean:message key="link.addToAddressBk"/>
         </html:link>
       </c:if>
     </c:if>
   </td>
<% // TODO: It would be more correct if this wasn't dependant on there being 5 header rows.
    if (message.getHeader("X-Image-Url") != null) {
        final String imageUrl = message.getHeader("X-Image-Url")[0];
%>
   <td class="lightBlueRow" rowspan="5" align="right">
     <div style="width: 48px; height: 48px; float: right; overflow: hidden;">
       <img src="<%= imageUrl %>" border="0" alt="<%= imageUrl %>" title="<%= imageUrl %>"/>
     </div>
   </td>
<%
    }
%>
  </tr>

  <tr>
   <th class="leftNavBar" width="100"><bean:message key="message.to"/>:</th>
   <td class="lightBlueRow">
<%
    Address toArray[] = message.getRecipients(Message.RecipientType.TO);
    if (toArray != null) {
        pageContext.setAttribute("toArray", toArray);
%>
   <c:forEach items="${toArray}" var="to" varStatus="index">
     <wm:write name="to" filter="false" formatKey="message.view.email.format" formatClassKey="message.email.formatter"/>
     <c:if test="${!index.last}">, </c:if>
   </c:forEach>
<%
    }
%>
   </td>
  </tr>

  <tr>
   <th class="leftNavBar" width="100"><bean:message key="message.cc"/>:</th>
   <td class="lightBlueRow">
<%
    Address ccArray[] = message.getRecipients(Message.RecipientType.CC);
    if (ccArray != null) {
        pageContext.setAttribute("ccArray", ccArray);
%>
   <c:forEach items="${ccArray}" var="cc" varStatus="index">
     <wm:write name="cc" filter="false" formatKey="message.view.email.format" formatClassKey="message.email.formatter"/>
     <c:if test="${!index.last}">, </c:if>
   </c:forEach>
   </td>
<%
    }
%>
  </tr>

  <tr>
   <th class="leftNavBar" width="100"><bean:message key="message.date"/>:</th>
   <td class="lightBlueRow">
     <c:if test="${!empty message.sentDate}">
       <bean:write name="message" property="sentDate" filter="true"/>
     </c:if>
   </td>
  </tr>

  <tr>
   <th class="leftNavBar" width="100"><bean:message key="message.subject"/>:</th>
   <td class="lightBlueRow">
    <c:choose>
     <c:when test="${!empty message.subject}">
      <bean:write name="message" property="subject" filter="true"/>
     </c:when>
     <c:otherwise>
      <i class="noSubject"><bean:message key="message.nosubject"/></i>
     </c:otherwise>
    </c:choose>
   </td>
  </tr>
 </table>

<table class="messageBody" cellspacing="0" cellpadding="2" width="100%" border="0">
<%
    String tileDefinition = Util.getDefinitionName(message.getContentType());
    Object o = message.getContent();
    request.setAttribute("body", o);

    if (request.getAttribute("realFolder") == null) {
        request.setAttribute("realFolder", message.getFolder());
    }

    if (request.getAttribute("realMessage") == null) {
        request.setAttribute("realMessage", message);
    }


    out.println("<!-- Start message.jsp: " + tileDefinition + " -->");
%><tiles:insert name="<%=tileDefinition%>"/><%
    out.println("<!-- End   message.jsp: " + message.getContentType() + "-->");
%>
</table>
<%
    if (folder != null) {
        Util.releaseFolder(folder);
    }
%>