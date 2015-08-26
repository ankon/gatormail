<%@page contentType="text/html" import="edu.ufl.osg.webmail.util.Util,
                                        javax.mail.Folder,
                                        java.util.ArrayList,
                                        java.util.Arrays,
                                        java.util.HashMap,
                                        java.util.List,
                                        java.util.Map"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<table class="headerTable" cellpadding="3" cellspacing="0" width="100%">
    <tr class="header">
        <th colspan="7"><bean:message key="folderManage.title"/></th>
    </tr>
    <tr class="subheader">
        <td><bean:message key="folderManage.table.folder"/></td>
        <td width="10%"><bean:message key="folderManage.table.numMessages"/></td>
        <td width="10%"><bean:message key="folderManage.table.unread"/></td>
        <td width="10%"><bean:message key="folderManage.table.subscribed"/></td>
        <td width="10%"><bean:message key="folderManage.table.modify"/></td>
        <td width="10%"><bean:message key="folderManage.table.delete"/></td>
        <td width="30%">&nbsp;</td>
    </tr>

    <c:forEach items="${folderBeanList}" var="folder" varStatus="folderStatus">
        <%
            Folder openFolder = null;
            int size = 0;
            List messageList = null;
            Folder folder = (Folder)pageContext.getAttribute("folder");

            try{
                folder = Util.getFolder(folder);
                if(folder != null){
                    messageList = new ArrayList(Arrays.asList(folder.getMessages()));
                }
            }
            finally{
                Util.releaseFolder(folder);
            }
            pageContext.setAttribute("messages", messageList);
            Map folderParams = new HashMap();
            folderParams.put("folder", folder.getFullName());
            pageContext.setAttribute("folderParams", folderParams);
            String unreadMessage = String.valueOf(folder.getUnreadMessageCount());
            pageContext.setAttribute("unreadMessage", unreadMessage);
        %>
        <c:choose>
            <c:when test="${folderStatus.index % 2 == 0}">
                <tr>
            </c:when>
            <c:otherwise>
                <tr class="altrow">
            </c:otherwise>
        </c:choose>
        <td><html:link forward="folder" paramId="folder" paramName="folder" paramProperty="fullName">
            <bean:write name="folder" property="fullName" filter="true"/></html:link>
        </td>
        <td align="left">
            <% // TODO Figure out how to predict when this will throw an exception.
                try {
            %>
            <bean:write name="folder" property="messageCount"/>
            <%
                } catch (Exception e) {
                    // Ignore the error
                }
            %>
        </td>
        <td align="left">
            <%=unreadMessage%>
        </td>
        <td align="left">
            <c:choose>
                <c:when test="${folder.subscribed}">
                    <bean:message key="folderManage.table.yes"/>
                </c:when>
                <c:otherwise>
                    <bean:message key="folderManage.table.no"/>
                </c:otherwise>
            </c:choose>
        </td>
        <td><html:link forward="folderManageModify" name="folderParams" scope="page">
            <bean:message key="folderManage.table.modify"/></html:link>
        </td>
        <td>
            <%
                if (!Util.isReservedFolder(folder.getFullName(), session)) {
            %>
            <html:link forward="deleteFolder" name="folderParams" scope="page">
                <bean:message key="folderManage.table.delete"/></html:link>
            <%
                }
            %>
        </td>
        <td width="30%">&nbsp;</td>
        </tr>
        <%
            Util.releaseFolder(folder);
        %>
    </c:forEach>

</table>
<bean:define id="folder" name="folder" type="javax.mail.Folder" scope="request"/>
<table class="headerTable" cellpadding="0" cellspacing="0" width="100%">
    <br/>
    <tr class="header">
        <th>
            <strong><bean:message key="folderManageModify.title"/>:</strong>
            <tr>
                <td>
                </td>
            </tr>

        </th>
    </tr>

    <%-- create new subfolder --%>
    <tr>
        <td>
            <html:form action="createFolder">
        <table class="headerTable" cellpadding="3" cellspacing="0" width="100%">
            <tr class="subheader">
                <td>
                    <bean:message key="folderManageModify.subFolder.headerFolder"/>
                </td>
                </tr>
	            <tr>
                <td>
                    <bean:message key="folderManageModify.subFolder.headerFolder"/>
                    <html:text styleId="newFolder" property="newFolder" maxlength="30"/>
                    <bean:message key="folderManageModify.subFolder.desc"/>
                    <select id="folder" name="folder">
                        <c:forEach items="${folderBeanList}" var="folder">
                            <option value="${folder.fullName}"><bean:write name="folder" property="fullName"/></option>
                        </c:forEach>
                    </select>
                </td>
            </tr>
        </table>

        <html:submit styleId="newFolderSubmit" styleClass="button">
            <bean:message key="button.folderManageModify.newFolder"/>
        </html:submit>
        </html:form>

        <script language="JavaScript" type="text/javascript">
        <!--
var newFolderSubmit = document.getElementById("newFolderSubmit");
var newFolder = document.getElementById("newFolder");

function updateButton() {
    newFolderSubmit.disabled = newFolder.value.length == 0;
};

newFolder.onchange = updateButton;
folderList.onchange = updateButton;

// Disable the button if JavaScript is enabled
updateButton();
//-->
</SCRIPT>

        <tr><td>&nbsp;</td></tr>
</table>
