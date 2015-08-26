<%@page contentType="text/html"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:if test="${empty(folderListFilterType) or folderListFilterType == 'none'}">
    <p>
        <table border="0" width="100%">
            <tr>
                <td align="center">
                    <html:form action="emptyFolder">
                        <bean:define id="folderFullName" name="folder" property="fullName" type="java.lang.String"/>
                        <bean:define id="folderName" name="folder" property="name" type="java.lang.String"/>
                        <html:hidden property="folderName" value="<%= folderFullName %>"/>
                        <html:hidden property="sure" value="false"/>
                        <html:hidden property="permanent" value="false"/>
                        <html:submit property="action" styleClass="button" titleKey="button.emptyFolder.title"><bean:message key="button.emptyFolder" arg0="<%= folderName %>"/></html:submit>
                    </html:form>
                </td>
            </tr>
        </table>
    </p>
</c:if>