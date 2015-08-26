<%@page contentType="text/html" import="java.io.InputStream,
                                        java.io.BufferedInputStream,
                                        java.io.BufferedReader,
                                        java.io.InputStreamReader,
                                        java.util.Map,
                                        java.util.HashMap,
                                        java.util.Set,
                                        java.util.Iterator,
                                        javax.mail.BodyPart"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%-- From: http://www.rfc-editor.org/rfc/rfc1894.txt
   This memo defines a MIME content-type that may be used by a message
   transfer agent (MTA) or electronic mail gateway to report the result
   of an attempt to deliver a message to one or more recipients.  This
   content-type is intended as a machine-processable replacement for the
   various types of delivery status notifications currently used in
   Internet electronic mail.

   TODO: Make this a table or something. eg:
--h2LHaruq106852.1048268213/smtp.ufl.edu
Content-Type: message/delivery-status

Reporting-MTA: dns; smtp.ufl.edu
Received-From-MTA: DNS; sp41en1.nerdc.ufl.edu
Arrival-Date: Fri, 21 Mar 2003 12:35:58 -0500

Final-Recipient: RFC822; sotoudeh@ut.ac.ir
Action: failed
Status: 5.1.1
Remote-MTA: DNS; chamran.ut.ac.ir
Diagnostic-Code: SMTP; 550 5.1.1 <sotoudeh@ut.ac.ir>... User unknown
Last-Attempt-Date: Fri, 21 Mar 2003 12:36:52 -0500

--h2LHaruq106852.1048268213/smtp.ufl.edu
--%>
<%
    Object o = request.getAttribute("body");
    InputStream in = null;
    if (o instanceof BodyPart) {
        in = (InputStream)((BodyPart)o).getContent();
    } else {
        // XXX Should we silently ignore these?
%>
<table class="messageBody" cellspacing="0" cellpadding="2" width="100%" border="0">
 <tr>
  <th class="leftNavBar" width="100">&nbsp;TODO:</th>
  <td class="messageMessageDeliveryStatus">
   <b>
    Unable to handle a: <%= o.getClass().getName() %> in delivery-status.jsp<br/>
    Please tell your system admin.
   </b>
  </td>
 </tr>
</table>
<%
        return;
    }

    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    String line;
    Map fields = new HashMap();
    while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.length() != 0) {
            String key = line.substring(0, line.indexOf(':'));
            String value = line.substring(line.indexOf(':') + 1);
            fields.put(key, value);
        }
    }
%>
<table class="messageBody" cellspacing="0" cellpadding="2" width="100%" border="0">
 <tr>
  <th class="leftNavBar" width="100">&nbsp;</th>
  <td class="darkBlueRow"><bean:message key="message.delivery-status"/></td>
 </tr>
 <tr>
  <th class="leftNavBar" width="100">&nbsp;</th>
  <td class="messageMessageDeliveryStatus">
   <table border="1" cellspacing="0" cellpadding="1">
<%
    Set keys = fields.keySet();
    Iterator iter = keys.iterator();
    while (iter.hasNext()) {
        String key = (String)iter.next();
        String value = (String)fields.get(key);
        out.println("<tr>");

        out.println("<th>");
        out.println(key);
        out.println("</th>");

        out.println("<td>");
        out.println(value);
        out.println("</td>");

        out.println("</tr>");
    }
%>
   </table>
  </td>
 </tr>
</table>
