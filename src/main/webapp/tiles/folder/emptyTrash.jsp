<%@ page import="javax.mail.Folder"%>
<%@ page import="edu.ufl.osg.webmail.util.Util"%>
<%@ page import="javax.mail.MessagingException"%>
<%@page contentType="text/html"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%
    String trashFullName = "INBOX.Trash";
    try {
        Folder f = Util.getFolder(session, "INBOX");
        Util.releaseFolder(f);
        f = f.getFolder("Trash");
        Util.releaseFolder(f);
        trashFullName = f.getFullName();
    } catch (MessagingException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    request.setAttribute("trashFullName", trashFullName);
%>
<p>
    <table border="0" width="100%">
        <tr>
            <td align="center">
                <html:form action="emptyFolder">
                    <html:hidden property="folderName" value="${trashFullName}"/>
                    <html:hidden property="sure" value="true"/>
                    <html:hidden property="permanent" value="true"/>
                    <html:submit property="action" styleClass="button" titleKey="button.emptyTrash.title"><bean:message key="button.emptyTrash"/></html:submit>
                </html:form>
            </td>
        </tr>
    </table>
</p>