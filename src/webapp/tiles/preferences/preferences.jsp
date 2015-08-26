<%@page contentType="text/html" import="java.util.List,
                                        edu.ufl.osg.webmail.util.Util,
                                        edu.ufl.osg.webmail.Constants,
                                        java.util.ArrayList"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/webmail" prefix="wm"%>

<html:form action="preferences">
<table class="headerTable" cellpadding="3" cellspacing="0" width="100%">
  <tr class="header">
    <th colspan="2"><bean:message key="preferences.title"/></th>
  </tr>

  <tr class="subheader">
    <th colspan="2" align="left">Name and Address</th>
  </tr>

  <tr>
    <th width="20%" align="right" valign="top">From name:</th>
    <td>
      <html:errors property="username"/>
      <html:text property="username" size="40"/>
      <div class="tip">The name displayed in the From field of messages you compose.</div>
    </td>
  </tr>

  <tr class="altrow">
    <th width="20%" align="right" valign="top">Reply To address:</th>
    <td>
      <html:errors property="replyTo"/>
      <html:text property="replyTo" size="40"/>
      <div class="tip">The address you want people to reply to if different than this one.</div>
    </td>
  </tr>

  <tr>
    <td colspan="2">&nbsp;</td>
  </tr>

  <tr class="subheader">
    <th colspan="2" align="left">Compose</th>
  </tr>

  <tr>
    <th wdith="20%" align="right" valign="top">Signature:</th>
    <td>
      <html:errors property="signature"/>
      <html:textarea property="signature" cols="78" rows="5"/>
      <div class="tip">
        Enter a custom signature to be attached when you compose messages.
        <p>
          You are encouraged to keep the "cut line", the two dashes and then a space. <br/>
          Many email clients reconize this as the start of your signature.
        </p>
      </div>
    </td>
  </tr>

  <tr class="altrow">
    <th width="20%" align="right" valign="top">Recipient Auto Complete:</th>
    <td>
      <html:checkbox property="autocomplete"/>
      <div class="tip">
       If checked, addresses in your address book will be autocompleted as you type them.
       <p>
         You must have at least one address in your address book for this to work.<br/>
         If your browser can remember previous recipients and you prefer that method then disable this feature.
       </p>
      </div>
    </td>
  </tr>

  <tr>
    <th width="20%" align="right" valign="top">Image URL:</th>
    <td>
<%
    if (request.getAttribute("X-Image-Url") != null && ((String)request.getAttribute("X-Image-Url")).length() > 0) {
        final String imageUrl = (String)request.getAttribute("X-Image-Url");
%>
     <div style="width: 48px; height: 48px; float: right; overflow: hidden;">
       <img src="<%= imageUrl %>" border="0" alt="<%= imageUrl %>" title="<%= imageUrl %>"/>
     </div>
<%
    }
%>
      <html:errors property="imageUrl"/>
      <html:text property="imageUrl" size="40"/>
      <div class="tip">
        Enter the full URL to a small image to be shown like a buddy icon for <br/>
        email if the reciepient's email client supports it.
        <p>
          The image should have a size of 48x48 which means buddy icons work well. <br/>
          When set a preview will be shown on the right.
        </p>
      </div>
    </td>
  </tr>

  <tr>
    <td colspan="2">&nbsp;</td>
  </tr>

  <tr class="subheader">
    <th colspan="2" align="left">Junk Mail</th>
  </tr>

  <tr>
    <th width="20%" align="right" valign="top">Junk Mail Flag Threshold:</th>
    <td>
<%
    final List junkThresholds = new ArrayList(16);
    for (int i=0; i <= 15; i++) {
        junkThresholds.add("" + i);
    }
    pageContext.setAttribute("junkThresholds", junkThresholds);
%>
      <html:errors property="junkThreshold"/>
      <html:select property="junkThreshold">
        <c:forEach items="${junkThresholds}" var="choice">
<%
    String choice = (String)pageContext.getAttribute("choice");
    String filterKey = "message.junk.threshold." + choice;
%>
          <html:option value="<%= choice %>"><bean:message key="<%= filterKey %>"/></html:option>
        </c:forEach>
      </html:select>
      <div class="tip">
       Set the junk score threshold to flag messages as junk. Lower thresholds <br/>
       are more likely to mark a message as spam.
       <p>
         This setting also controls the message list view filter threshold.
       </p>
      </div>
    </td>
  </tr>

  <tr class="altrow">
    <th width="20%" align="right" valign="top">Auto Filter Junk Mail:</th>
    <td>
      <html:checkbox property="junkSieveEnabled"/>
      <div class="tip">
       If checked, move messages with a Junk score higher than the threshold above into the "Junk" folder.
       <p>
        You will need to empty the Junk folder every now and then to prevent your mailbox from filling up.<br/>
        If the "Junk" folder doesn't exist it will be created for you.<br/>
        Enabling this will also affect message delivery in stand alone email clients.
       </p>
      </div>
    </td>
  </tr>

  <tr>
    <td colspan="2">&nbsp;</td>
  </tr>

  <tr class="subheader">
    <th colspan="2" align="left">Vacation Auto-Responder</th>
  </tr>

  <tr>
    <th width="20%" align="right" valign="top">Vacation Message:</th>
    <td>
        <html:errors property="vacationMessage"/>
        <html:textarea property="vacationMessage" cols="78" rows="5"/>

        <div class="tip">
            Keep the vacation message short and to the point.
            It is limited to about 1,000 characters.
        </div>
    </td>
  </tr>

  <tr class="altrow">
    <th width="20%" align="right" valign="top">Enable VacationAuto-Responder:</th>
    <td>
      <html:checkbox property="vacationSieveEnabled"/>
      <div class="tip">
       If checked, messages you receive will trigger an automatic response with the message above.
      </div>
    </td>
  </tr>

  <tr>
    <td colspan="2">&nbsp;</td>
  </tr>

  <tr class="subheader">
    <th colspan="2" align="left">View</th>
  </tr>

  <tr>
    <th width="20%" align="right" valign="top">Show Threads:</th>
    <td>
      <html:errors property="threading"/>
      <html:checkbox property="threading"/>
      <div class="tip">
       If checked, highlight threads in the message list as you move the mouse over the list.
       <p>
        A few web browsers can get very sluggish with this enabled.
       </p>
      </div>
    </td>
  </tr>

  <tr class="altrow">
    <th width="20%" align="right" valign="top">Hide Page Headers:</th>
    <td>
      <html:errors property="hideHeader"/>
      <html:checkbox property="hideHeader"/>
      <div class="tip">
       If checked, this hides the logo and other page header contents.
      </div>
    </td>
  </tr>

  <tr>
    <td colspan="2">&nbsp;</td>
  </tr>

  <tr>
    <td>&nbsp;</td>
    <td>
      <html:submit property="action"><bean:message key="button.savePreferences"/></html:submit>
    </td>
  </tr>


</table>

</html:form>