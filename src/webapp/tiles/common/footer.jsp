<%@page contentType="text/html" import="java.util.Date"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<p>
<table width="100%">
 <tr>
  <td class="footer" align="center">
   Copyright &copy; 2002-2005 <a href="http://Sandy.McArthur.org/" style="color:gray;text-decoration:none">William A. McArthur, Jr.</a> <br/>
   <br/>
   Copyright &copy; 2003-2007 <html:link href="http://open-systems.ufl.edu/">The Open Systems Group</html:link> |
   <html:link href="http://www.cns.ufl.edu/">Computing and Networking Services</html:link> |
   <html:link href="http://www.ufl.edu/">University of Florida</html:link> <br/>
   <br/>
   Site maintained by <html:link href="http://open-systems.ufl.edu/">The Open Systems Group</html:link> /
   Last Modified: @DATE@
   <br/>
   <br/>
<%
    Long requestStartTime = (Long)request.getAttribute("requestStartTime");
    if (requestStartTime != null) {
    out.print("Request took: ");
    out.print((System.currentTimeMillis() - requestStartTime.longValue()) / 1000f);
    out.print(" seconds at " + new Date().toString());
    }
%>
  </td>
<%--
  <td width="100">
   <html:link href="http://jakarta.apache.org/struts/">
    <html:img page="/struts-power-trans.gif" border="0" title="Powered by Struts"/>
   </html:link>
  </td>
--%>
 </tr>
</table>
</p>