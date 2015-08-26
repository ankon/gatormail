<%@ page import="javax.mail.Message,
                 edu.ufl.osg.webmail.util.Util"%>
<%@page contentType="text/html; charset=utf-8"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%
    Util.getFolder(((Message)request.getAttribute("message")).getFolder());
%>
<tiles:insert definition="messageLayout">
  <tiles:put name="messageName"><%
      String subj = ((Message)request.getAttribute("message")).getSubject();
      if (subj == null) {
          subj = "(No Subject)";
      }
      out.print(subj);
%></tiles:put>
</tiles:insert>
<%
    Util.releaseFolder(((Message)request.getAttribute("message")).getFolder());
%>