<%@page contentType="text/html" import="java.util.Map,
                                        java.util.HashMap,
                                        javax.mail.Folder"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/webmail" prefix="wm"%>

<p>
    <strong><bean:message key="confirmEmptyFolder.header"/>:</strong>
    <bean:write name="folder" property="fullName" filter="true"/>
</p>

<p>
    <bean:message key="confirmEmptyFolder.listIntro"/>:
    <br/>
<blockquote>
    <table class="headerTable" cellpadding="3" cellspacing="0">
        <tr class="subheader">
            <td><bean:message key="folderManage.table.folder"/></td>
            <td><bean:message key="folderManage.table.numMessages"/></td>
        </tr>
        <%
            final Folder folder = (Folder)request.getAttribute("folder");
            final Map folderParams = new HashMap();
            folderParams.put("folder", folder.getFullName());
            pageContext.setAttribute("folderParams", folderParams);
        %>
        <tr>
            <td><html:link forward="folder" paramId="folder" paramName="folder" paramProperty="fullName">
                <bean:write name="folder" property="fullName" filter="true"/>
            </html:link>
            </td>
            <td align="right"><bean:write name="folder" property="messageCount"/></td>
        </tr>
    </table>
</blockquote>

<html:form action="emptyFolder">
    <p>
        <bean:message key="confirmEmptyFolder.delete.now"/>
<%
    boolean perm = Boolean.getBoolean((String)request.getAttribute("perm"));
%>
        <select name="permanent">
            <option value="false" <%= !perm ? "selected=\"selected\"" : "" %>><bean:message key="confirmEmptyFolder.perm.false"/></option>
            <option value="true" <%= perm ? "selected=\"selected\"" : "" %>><bean:message key="confirmEmptyFolder.perm.true"/></option>
        </select>
    </p>
    <p><bean:message key="confirmEmptyFolder.prompt"/></p>

    <bean:define id="folderName" name="folder" property="fullName" scope="request" type="java.lang.String"/>

    <p>

    <html:hidden property="folderName" value="<%= folderName %>"/>
    <html:hidden property="sure" value="true"/>
    <html:submit property="action" styleClass="button">
        <bean:message key="button.cancelEmptyFolder"/>
    </html:submit>
    &nbsp;&nbsp;
    <html:submit property="action" styleClass="button">
        <bean:message key="button.confirmEmptyFolder"/>
    </html:submit>
</html:form>
</p>