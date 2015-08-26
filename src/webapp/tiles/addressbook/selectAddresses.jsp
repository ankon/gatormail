<%@page contentType="text/html"
        import="edu.ufl.osg.webmail.Constants,
                edu.ufl.osg.webmail.data.AddressList,
                edu.ufl.osg.webmail.util.Util,
                java.util.*,
                javax.mail.*,
                javax.mail.internet.*,
                org.apache.struts.action.ActionErrors,
                org.apache.struts.upload.FormFile"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>

<script language="JavaScript" type="text/javascript">
<!--
function toggleAll(checkBox) {
  if (checkBox.checked == true) {
     checkAll(checkBox, true);
  }
  else {
     checkAll(checkBox, false);
  }
}

function checkAll(checkBox, isChecked) {
   var length = document.addressesForm.elements.length;
   for (var i=0; i < length; i++) {
      var el = document.addressesForm.elements[i];
      if (el.name == "isSelected") {
         el.checked = isChecked;
      }
   }
   checkBox.checked = isChecked;
}

function uncheckCheckAll(form) {
    form.checkAllBox.checked=false;
}
//-->
</script>

<%
   Map messageParams = (Map)request.getAttribute("messageParams");
   pageContext.setAttribute("uid", (messageParams.get("uid") == null) ? "" : messageParams.get("uid").toString());
   pageContext.setAttribute("messageNumber", (messageParams.get("messageNumber") == null) ? "" : messageParams.get("messageNumber").toString());
   pageContext.setAttribute("sort",(messageParams.get("sort")== null) ? "": messageParams.get("sort").toString());
%>

<% try { %>
<html:form action="saveAddresses">

<bean:define id="folderFullName" name="folderFullName" scope="request" type="java.lang.String"/>
<html:hidden property="folder" value="<%=folderFullName%>"/>
<c:choose>
 <c:when test="${! empty messageParams.uid}">
  <bean:define id="uid" name="uid" scope="page" type="java.lang.String"/>
  <html:hidden property="uid" value="<%=uid%>"/>
 </c:when>
 <c:otherwise>
  <bean:define id="messageNumber" name="messageNumber" scope="page" type="java.lang.String"/>
  <html:hidden property="messageNumber" value="<%=messageNumber%>"/>
 </c:otherwise>
</c:choose>

<p><html:link forward="message" name="messageParams"><bean:message key="link.backToMessage"/></html:link></p>

<bean:define id="usage" name="<%=Constants.ADDRESSBK_USAGE%>" scope="request" type="java.lang.String"/>
<bean:define id="limit" name="<%=Constants.ADDRESSBK_LIMIT%>" scope="request" type="java.lang.String"/>
<p><bean:message key="addressbook.limit.message" arg0="<%=usage%>" arg1="<%=limit%>"/></p>

<p><bean:message key="selectAddresses.introduction"/></p>

<table class="headerTable" cellspacing="0" cellpadding="3">

  <tr class="header">
    <th>
	<input type="checkbox" name="checkAllBox" onclick="toggleAll(this)" title="<%=Util.getFromBundle("label.title.checkAll")%>"/>
    </th>
    <th><bean:message key="label.email"/></th>
    <th><bean:message key="label.name"/></th>
  </tr>

<%
  AddressList addressList = Util.getAddressList(session);
  String[] values = request.getParameterValues(Constants.IS_SELECTED);
  if (values != null) {
     Arrays.sort(values); // sort for binary search
  }
%>
  <c:forEach items="${saveAddressList}" var="iAddress" varStatus="index">
    <%
       InternetAddress iAddress = (InternetAddress)pageContext.getAttribute("iAddress");
       Boolean alreadySaved = (addressList.contains(iAddress)) ? Boolean.TRUE : Boolean.FALSE;
       pageContext.setAttribute("alreadySaved", alreadySaved);
       String entryEmail = (iAddress.getAddress() == null) ? "" : iAddress.getAddress();
       pageContext.setAttribute("entryEmail", entryEmail);
       String entryName = (iAddress.getPersonal() == null) ? "" : iAddress.getPersonal();
       entryName = (!"".equals(entryName)) ? entryName : entryEmail;
       pageContext.setAttribute("entryName", entryName);
    %>
    <c:choose>
     <c:when test="${folderStatus.index % 2 == 0}">
      <tr>
     </c:when>
     <c:otherwise>
      <tr class="altrow">
     </c:otherwise>
    </c:choose>
      <td valign="top">
        <c:if test="${!alreadySaved}">
          <input type="checkbox" name="isSelected" value="${index.index}" onclick="uncheckCheckAll(this.form)"/>
        </c:if>
      </td>
      <td>
	  <html:hidden property="email" value="${entryEmail}"/>
	   <c:out value="${entryEmail}"/>
      </td>
      <td>
       <c:choose>
        <c:when test="${alreadySaved}">
	     <html:hidden property="name" value="${entryName}"/>
	     <c:out value="${entryName}"/>
        </c:when>
        <c:otherwise>
         <html:text property="name" size="30" maxlength="40" value="${entryName}"/>
        </c:otherwise>
       </c:choose>
      </td>
    </tr>
  </c:forEach>

</table>

<p><html:submit><bean:message key="button.saveToAddressBk"/></html:submit></p>

</html:form>

<% } catch (Exception e) {
e.printStackTrace();
}
%>
