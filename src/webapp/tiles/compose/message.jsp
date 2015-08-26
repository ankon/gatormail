<%@page contentType="text/html" import="java.util.List,
                                        edu.ufl.osg.webmail.util.Util,
                                        edu.ufl.osg.webmail.Constants"%>
<%@ page import="edu.ufl.osg.webmail.prefs.PreferencesProvider" %>
<%@ page import="edu.ufl.osg.webmail.User" %>
<%@ page import="java.util.Properties" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/webmail" prefix="wm"%>

<%-- Dependencies --%>
<script type="text/javascript" src="yui/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="yui/dom/dom-min.js"></script>
<script type="text/javascript" src="yui/event/event-min.js"></script>


<%
    final User user = Util.getUser(session);
    boolean autocomplete = true;
    if (user != null) {
        final PreferencesProvider pp = (PreferencesProvider)application.getAttribute(Constants.PREFERENCES_PROVIDER);
        final Properties prefs = pp.getPreferences(user, session);
        autocomplete = Boolean.valueOf(prefs.getProperty("compose.recipients.autocomplete", "true")).booleanValue();
    }
    if (autocomplete) {
%>

<%-- OPTIONAL: Animation (required only if enabling animation) --%>
<script type="text/javascript" src="yui/animation/animation-min.js"></script>

<%-- Source file --%>
<script type="text/javascript" src="yui/autocomplete/autocomplete-min.js"></script>
<script language="JavaScript" type="text/javascript">
<!--
    function populateAddress(field) {
        var contacts = document.composeForm.contacts;
        var length = contacts.length;

        while( length-- ) {
            if (contacts.options[length].selected) {
                if (field.value == "") {
                    field.value += contacts.options[length].value;

                } else if (contacts.options[length].value != ""
                        && field.value.search(" " + contacts.options[length].value + ",") == -1
                        && field.value.search("^" + contacts.options[length].value) == -1
                        && field.value.search(" " + contacts.options[length].value + "$") == -1) {
                    field.value += ", " + contacts.options[length].value;
                }
            }
        }

       return false;
    }

                    <c:choose>
                     <c:when test="${!empty addressList}">
    var addressList = [
                      <c:forEach items="${addressList}" var="iAddress">
    {p:"<c:out value="${iAddress.personal}"/>", a:"<c:out value="${iAddress.address}"/>"},
                      </c:forEach>
    ];
                     </c:when>
                     <c:otherwise>
    var addressList = [];
                     </c:otherwise>
                    </c:choose>
    var addressListFunction = function (query) {
        var results = [];
        if (query && query.length > 0) {
            var queryLower = query.toLowerCase();
            for (var i=0; i < addressList.length; i++) {
                var addressItem = addressList[i];
                if (addressItem) {
                    var personal = addressItem["p"];
                    var address = addressItem["a"];
                    var result = [];
                    var found = false;
                    if (address && address.toLowerCase().indexOf(queryLower) === 0) {
                        result[0] = address;
                        result[1] = personal;
                        results[results.length] = result;
                        found = true;
                    }
                    if (!found && personal && personal.toLowerCase().indexOf(queryLower) >= 0) {
                        var idx = personal.toLowerCase().indexOf(queryLower);
                        if (idx === 0 || personal.toLowerCase().indexOf(" " + queryLower) >= 0) {
                            result[0] = address;
                            result[1] = personal;
                            results[results.length] = result;
                            found = true;
                        }
                    }
                } else {
                    delete addressList[i];
                }
            }
        }
        return results;
    }
    var addressListDataSource = new YAHOO.widget.DS_JSFunction(addressListFunction);
    var addressListFormatter = function (result, query) {
        var markup = "";
        if (result[1]) {
            markup = "\"" + result[1] + "\" ";
        }
        markup += "&lt;" + result[0] + "&gt;";
        try {
            var re = new RegExp("(" + query + ")", "i");
            markup = markup.replace(re, "<b>$1</b>");
        } catch (ex) {
            // ignore
        }
        return markup;
    }
    function initAutoComplete(elem, container) {
        var ac = new YAHOO.widget.AutoComplete(elem, container, addressListDataSource);
        ac.delimChar = ",";
        ac.maxResultsDisplayed = 20;
        ac.queryDelay = 0.1;
        ac.animSpeed = 0.2;
        ac.formatResult = addressListFormatter;
        return ac;
    }
// -->
</script>
<%
    } else { // if (autocomplete)
%>
<script>
<!--
    function initAutoComplete(elem, container) {
        return null;
    }
// -->
</script>
<%
    } // if (autocomplete) else
%>
<html:form method="post" action="modifyCompose" enctype="multipart/form-data" focus="to">
<html:hidden property="composeKey"/>
<style type="text/css">
    #toAutoComplete, #ccAutoComplete, #bccAutoComplete {position:relative;width:100%;height:2em;}/* set width of widget here*/
    #toAutoComplete {z-index:9000} /* for IE z-index of absolute divs inside relative divs issue */
    #ccAutoComplete {z-index:8000} /* for IE z-index of absolute divs inside relative divs issue */
    #bccAutoComplete {z-index:7000} /* for IE z-index of absolute divs inside relative divs issue */
    #toinput, #ccinput, #bccinput {_position:absolute;width:100%;Xheight:1.4em;z-index:0;} /* abs for ie quirks */

    #toCompleteContainer, #ccCompleteContainer, #bccCompleteContainer {position:absolute;top:1.7em;width:95%}
    #toCompleteContainer .yui-ac-content, #ccCompleteContainer .yui-ac-content, #bccCompleteContainer .yui-ac-content {position:absolute;width:100%;border:1px solid #404040;background:#fff;overflow:hidden;z-index:9050;}
    #toCompleteContainer .yui-ac-shadow, #ccCompleteContainer .yui-ac-shadow, #bccCompleteContainer .yui-ac-shadow {position:absolute;margin:.3em;width:100%;background:#a0a0a0;z-index:9049;}
    #toCompleteContainer ul, #ccCompleteContainer ul, #bccCompleteContainer ul {padding:0 1px 0;width:100%; margin:0.3em;}
    #toCompleteContainer li, #ccCompleteContainer li, #bccCompleteContainer li {padding:0 5px;cursor:default;white-space:nowrap;}
    #toCompleteContainer li.yui-ac-highlight, #ccCompleteContainer li.yui-ac-highlight, #bccCompleteContainer li.yui-ac-highlight {background:#ff0;}
    #toCompleteContainer li.yui-ac-prehighlight, #ccCompleteContainer li.yui-ac-prehighlight, #bccCompleteContainer li.yui-ac-prehighlight {background:#FFFFCC;}
</style>

<script language="JavaScript" type="text/javascript">
<!--
var preventSubmit = function (e) {
    if (YAHOO.util.Event.getCharCode(e) == 13) {
        YAHOO.util.Event.preventDefault(e);
    }
}
//-->
</script>
<table width="100%" cellpadding="2" cellspacing="0">
 <tr class="lightBlueRow">
  <td width="15%" align="right" class="composeHeaderTitle">&nbsp;</td>
  <td colspan="4" class="darkBlueRow">
    <table>
     <tr>
      <td class="darkBlueRow">
       <html:submit property="action" styleClass="button" accesskey="s">
        <bean:message key="button.send"/>
       </html:submit>
       <html:submit property="action" styleClass="button">
        <bean:message key="button.cancelMessage"/>
       </html:submit>
      </td>
      <td class="darkBlueRow" colspan="2">&nbsp;</td>
     </tr>
    </table>
  </td>
 </tr>
  <tr class="lightBlueRow">
    <td width="15%" align="right" class="composeHeaderTitle"><bean:message key="message.to"/>:</td>
    <td colspan="3">
     <div id="toAutoComplete">
       <html:text property="to" styleId="toInput" size="63" style="width : 99%" tabindex="10"/>
       <div id="toCompleteContainer"></div>
     </div>
    </td>
    <td valign="top" rowspan="7" width="200">
      <table align="center" width="100%">
        <tr>
          <td align="center">
           <noscript>
             <div style="color : red;"><bean:message key="addressbook.control.noscript"/></div>
           </noscript>
           <bean:message key="addressbook.control.message"/>
          </td>
        </tr>
        <tr>
          <td align="center">
            <input type="button" class="button" value="To" onclick="populateAddress(this.form.to)" accesskey="t">
            <input type="button" class="button" value="CC" onclick="populateAddress(this.form.cc)" accesskey="c">
            <input type="button" class="button" value="BCC" onclick="populateAddress(this.form.bcc)" accesskey="b">
          </td>
        </tr>
        <tr>
          <td align="center">
            <select name="contacts" multiple="multiple" size="27" style="width : 200px;" onDblClick="populateAddress(this.form.to)">
<%-- Show empty address book message. --%>
            <c:choose>
             <c:when test="${!empty addressList}">
              <c:forEach items="${addressList}" var="iAddress">
               <option value="${iAddress.address}" title="${iAddress.address}">
                <c:out value="${iAddress.personal}"/>
               </option>
              </c:forEach>
             </c:when>
             <c:otherwise>
               <option value=""><bean:message key="addressbook.control.empty1"/></option>
               <option value=""><bean:message key="addressbook.control.empty2"/></option>
               <option value=""><bean:message key="addressbook.control.empty3"/></option>
               <option value=""><bean:message key="addressbook.control.empty4"/></option>
             </c:otherwise>
            </c:choose>
            </select>
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr class="lightBlueRow">
    <td width="15%" align="right" class="composeHeaderTitle"><bean:message key="message.cc"/>:</td>
    <td colspan="3">
        <div id="ccAutoComplete">
          <html:text property="cc" styleId="ccInput" size="63" style="width : 99%" tabindex="20"/>
          <div id="ccCompleteContainer"></div>
        </div>
    </td>
  </tr>
  <tr class="lightBlueRow">
    <td width="15%" align="right" class="composeHeaderTitle"><bean:message key="message.bcc"/>:</td>
    <td colspan="3">
        <div id="bccAutoComplete">
            <html:text property="bcc" styleId="bccInput" size="63" style="width : 99%" tabindex="30"/>
          <div id="bccCompleteContainer"></div>
        </div>
        <script type="text/javascript">
            var toAutoComplete = initAutoComplete("toInput","toCompleteContainer");
            var ccAutoComplete = initAutoComplete("ccInput","ccCompleteContainer");
            var bccAutoComplete = initAutoComplete("bccInput","bccCompleteContainer");

            YAHOO.util.Event.addListener(["toInput", "ccInput", "bccInput"], "keypress", preventSubmit);
        </script>
    </td>
  </tr>
  <tr class="lightBlueRow">
    <td width="15%" align="right" class="composeHeaderTitle"><bean:message key="message.subject"/>:</td>
    <td colspan="3">
     <html:text property="subject" size="63" style="width : 99%" tabindex="40"/>
    </td>
  </tr>
  <tr class="lightBlueRow">
    <td width="15%" align="right" class="composeHeaderTitle"><bean:message key="compose.attachment.upload"/>:</td>
    <td colspan="3">
       <html:file property="attachment" accesskey="f"/>
       <html:submit property="action" styleClass="button"><bean:message key="button.attachment.upload"/></html:submit>
    </td>
  </tr>
 <!-- show attachments -->
 <bean:define id="composeKey" name="composeForm" property="composeKey" type="java.lang.String"/>
  <tr class="lightBlueRow">
   <td width="15%" align="right" class="composeHeaderTitle">&nbsp;</td>
   <td colspan="3">
<%
  final List attachList = Util.getAttachList(composeKey, session);
  if (attachList != null && attachList.size() > 0) {
     pageContext.setAttribute("attachList", attachList);
%>
    <table width="575" class="messageHeader" cellpadding="3">
      <tr class="darkBlueRow">
       <th><bean:message key="compose.name"/></th>
       <th><bean:message key="compose.type"/></th>
       <th><bean:message key="compose.size"/></th>
       <th>
        <html:submit property="action" styleClass="button">
         <bean:message key="button.attachment.delete"/>
        </html:submit>
       </th>
      </tr>
      <c:forEach items="${attachList}" var="attachObj">
       <tr>
        <td><c:out value="${attachObj.fileName}"/></td>
        <td><c:out value="${attachObj.displayContentType}"/></td>
        <td><wm:write name="attachObj" property="size" filter="false"
	      formatKey="general.size.format" formatClassKey="general.size.formatter"/>
        </td>
        <td>
	     <input type="checkbox" name="<%=Constants.DELETE_ATTACHMENT%>"
	     value="<c:out value="${attachObj.tempName}"/>"/>
        </td>
       </tr>
      </c:forEach>
    </table>
<!--   </td>
  </tr>
-->
 <%
    }
 %>
 <!-- end show attachments ->
 <tr class="lightBlueRow">
  <td width="15%" align="right" class="composeHeaderTitle">&nbsp;</td>
  <td colspan="3">
-->
    <table>
     <tr>
      <td><html:checkbox property="copyToSent" styleId="copyToSent" titleKey="compose.copyToSent" accesskey="c"/></td>
      <td><label for="copyToSent"><bean:message key="compose.copyToSent"/></label></td>
     </tr>
    </table>
  </td>
 </tr>

 <tr class="lightBlueRow">
  <td width="15%" align="right" class="composeHeaderTitle">&nbsp;</td>
  <td colspan="3">
    <html:textarea property="body"
          cols="<%= String.valueOf(Constants.COMPOSE_BODY_WIDTH) %>" rows="20" style="width : 99%" tabindex="50"/>
  </td>
 </tr>
 <tr class="lightBlueRow">
  <td width="15%" align="right" class="composeHeaderTitle">&nbsp;</td>
  <td colspan="4" class="darkBlueRow">
    <table>
     <tr>
      <td class="darkBlueRow">
       <html:submit property="action" styleClass="button">
        <bean:message key="button.send"/>
       </html:submit>
      </td>
      <td class="darkBlueRow" colspan="2">&nbsp;</td>
     </tr>
    </table>
  </td>
 </tr>
 </table>
</html:form>
