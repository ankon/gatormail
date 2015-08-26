<%@page contentType="text/html" import="javax.mail.Folder,
                                        edu.ufl.osg.webmail.util.Util,
                                        java.util.Collection,
                                        java.util.ArrayList,
                                        edu.ufl.osg.webmail.wrappers.MessageWrapper,
                                        java.util.Map,
                                        javax.mail.Flags,
                                        java.util.HashMap,
                                        org.apache.struts.util.ResponseUtils,
                                        javax.mail.internet.MimeMessage,
                                        java.util.StringTokenizer,
                                        java.util.Set,
                                        java.util.HashSet,
                                        java.util.Arrays,
                                        java.util.Iterator,
                                        javax.mail.UIDFolder,
                                        edu.ufl.osg.webmail.prefs.PreferencesProvider,
                                        edu.ufl.osg.webmail.Constants,
                                        java.util.Properties,
                                        edu.ufl.osg.webmail.User,
                                        javax.mail.Message"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fn" prefix="fn"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%@taglib uri="/tags/webmail" prefix="wm"%>
<%
    Folder folder = (Folder)request.getAttribute("folder");
    String sort = (String)request.getAttribute("folderSort");
    folder = Util.getFolder(folder);
    
    


    Collection columns = new ArrayList(4);
    columns.add("flags");
    if (folder.getName().toUpperCase().startsWith("SENT")) {
        columns.add("to");
    } else {
        columns.add("from");
    }
    columns.add("subject");
    columns.add("date");
    columns.add("size");
    pageContext.setAttribute("columns", columns);

    final User user = Util.getUser(session);
    final PreferencesProvider pp = (PreferencesProvider)application.getAttribute(Constants.PREFERENCES_PROVIDER);
    final Properties prefs = pp.getPreferences(user, session);
    final boolean threadingEnabled = Boolean.valueOf(prefs.getProperty("folder.list.threading", "false")).booleanValue();
    final int junkThreshold = Integer.valueOf(prefs.getProperty("message.junk.threshold", "8")).intValue();
    final String junkPattern;
    if (junkThreshold > 0) {
        junkPattern = "***************************************".substring(0,junkThreshold);
    } else {
        junkPattern = null;
    }
%>
<html:form action="modifyFolder">
<bean:define id="folderName" name="folder" property="fullName" type="java.lang.String"/>
<html:hidden property="folder" value="<%=folderName%>"/>
<html:hidden property="page" value="${folderListPage}"/>
<html:hidden property="sort" value="${folderSort}"/>


<table width="100%" border="0" cellpadding="4" cellspacing="0" class="folderMessageList">
 <tr class="lightBlueRow">
  <td align="left">
   <c:choose>
    <c:when test="${folder.fullName != 'INBOX.Trash' and folder.fullName != 'INBOX/Trash'}">
     <html:submit property="action" styleClass="button">
      <bean:message key="button.delete"/>
     </html:submit>
    </c:when>
    <c:when test="${folder.fullName == 'INBOX.Trash' or folder.fullName == 'INBOX/Trash'}">
     <html:hidden property="deleteForever" value="true"/>
     <html:submit property="action" styleClass="button">
      <bean:message key="button.deleteForever"/>
     </html:submit>
    </c:when>
   </c:choose>
  </td>
  <td align="right">
   <tiles:insert page="/tiles/common/moveCopy.jsp" flush="true"/>
  </td>
 </tr>
</table>

<script language="JavaScript" type="text/javascript">
<!--
function toggleAll(checkBox) {
  checkAll(checkBox, checkBox.checked);
}

function checkAll(checkBox, isChecked) {
   var length = document.modifyFolderForm.elements.length;
   for (var i=0; i < length; i++) {
      var el = document.modifyFolderForm.elements[i];
      if (el.name == "uid" || el.name == "messageNumber") {
         el.checked = isChecked;
         ts(el);
      }
   }
   checkBox.checked = isChecked;
}

function uncheckCheckAll(form) {
    form.checkAllBox.checked=false;
}

function buildCSS(src, part, adding) {
    var css = "message";

    if (src.search("Seen") != -1) {
        css += "Seen";
    } else if (src.search("Unread") != -1) {
        css += "Unread";
    }

    if (part == "Selected") {
        if (adding) css += "Selected";
    } else {
        if (src.search("Selected") != -1) css += "Selected";
    }

    if (part == "Threaded") {
        if (adding) css += "Threaded";
    } else {
        if (src.search("Threaded") != -1) css += "Threaded";
    }

    return css;
}

function updateThread(node, part, adding) {
    var nodeId = node.id;
    node.className = buildCSS(node.className, part, adding);
    var refs = messageThreads[nodeId];
    if (refs) {
        for (var i = 0; i < refs.length; i++) {
            var ref = refs[i];
            var row;
            if (typeof ref == "object") {
                row = ref;
            } else if (typeof ref == "string") {
                row = document.getElementById(refs[i]);
            }
            if (row) row.className = buildCSS(row.className, part, adding);
        }
    }
}

// Toggle Selected
function ts(checkBox) {
    var row = null;
    if (checkBox.parentNode && checkBox.parentNode.parentNode) {
        row = checkBox.parentNode.parentNode;
    } else if (checkBox.parentElement && checkBox.parentElement.parentElement) {
        row = checkBox.parentNode.parentNode;
    }

    if (row) {
        row.className = buildCSS(row.className, "Selected", checkBox.checked);
    }
}

// Show Thread
function st(node) {
    if (<%= threadingEnabled %>) {
        updateThread(node, "Threaded", true);
    }
}

// Hide Thread
function ht(node) {
    if (<%= threadingEnabled %>) {
        updateThread(node, "Threaded", false);
    }
}

var messageThreads = new Object();
//-->
</script>

<table class="folderMessageList" width="100%" cellpadding="3" cellspacing="0" border="0">
 <tr class="folderMessageListHeader">
  <th>
   <input type="checkbox" name="checkAllBox" id="checkAllBox" onclick="toggleAll(this)" title="Check All Messages"/>
  </th>
<c:forEach items="${columns}" var="column" varStatus="columnStatus">
 <th valign="middle" >
  <c:choose>
  <c:when test="${column == 'date'}">
        <c:choose>
        <c:when test="${folderSort == 'dateDN'}">
            
            <a href= "folder.do?folder=<%=folderName%>&amp;sort=<c:out value="dateUP"/> " title="Sort by Date" >Date</a>       
            <html:img page="/sort-down.gif" border="0" align="absmiddle"/>
        </c:when>
        <c:when test="${folderSort == 'dateUP'}">
            <a href= "folder.do?folder=<%=folderName%>&amp;sort=<c:out value="dateDN"/> " title="Sort by Date" >Date</a>       
            <html:img page="/sort-up.gif" border="0" align="absmiddle"/>
        </c:when>
        <c:otherwise>
            <a href= "folder.do?folder=<%=folderName%>&amp;sort=<c:out value="dateDN"/> " title="Sort by Date" >Date</a> 
        </c:otherwise>
        </c:choose>
   </c:when>    
   <c:when test="${column == 'flags'}">
        <bean:message key="message.flags"/>
   </c:when>
   <c:when test="${column == 'from'}">
        <c:choose>
        <c:when test="${folderSort == 'fromUP'}">
            <a href= "folder.do?folder=<%=folderName%>&amp;sort=<c:out value="fromDN"/> " title="Sort by Sender" >Sender</a>       
            <html:img page="/sort-up.gif" border="0" align="absmiddle"/>
        </c:when>
        <c:when test="${folderSort == 'fromDN'}">
            <a href= "folder.do?folder=<%=folderName%>&amp;sort=<c:out value="fromUP"/> " title="Sort by Sender" >Sender</a>       
            <html:img page="/sort-down.gif" border="0" align="absmiddle"/>
        </c:when>
        <c:otherwise>
            <a href= "folder.do?folder=<%=folderName%>&amp;sort=<c:out value="fromDN"/> " title="Sort by Sender" >Sender</a> 
        </c:otherwise>
        </c:choose>
   </c:when>
   <c:when test="${column == 'size'}">
        <c:choose>
        <c:when test="${folderSort == 'sizeUP'}">
            <a href= "folder.do?folder=<%=folderName%>&amp;sort=<c:out value="sizeDN"/> " title="Sort by Size" >Size</a>       
            <html:img page="/sort-up.gif" border="0" align="absmiddle"/>
        </c:when>
        <c:when test="${folderSort == 'sizeDN'}">
            <a href= "folder.do?folder=<%=folderName%>&amp;sort=<c:out value="sizeUP"/> " title="Sort by Size" >Size</a>       
            <html:img page="/sort-down.gif" border="0" align="absmiddle"/>
        </c:when>
        <c:otherwise>
            <a href= "folder.do?folder=<%=folderName%>&amp;sort=<c:out value="sizeDN"/> " title="Sort by Size" >Size</a> 
        </c:otherwise>
        </c:choose>
   </c:when>
   <c:when test="${column == 'subject'}">
        <c:choose>
        <c:when test="${folderSort == 'subUP'}">
            <a href= "folder.do?folder=<%=folderName%>&amp;sort=<c:out value="subDN"/> " title="Sort by Subject" >Subject</a>       
            <html:img page="/sort-up.gif" border="0" align="absmiddle"/>
        </c:when>
        <c:when test="${folderSort == 'subDN'}">
            <a href= "folder.do?folder=<%=folderName%>&amp;sort=<c:out value="subUP"/> " title="Sort by Subject" >Subject</a>       
            <html:img page="/sort-down.gif" border="0" align="absmiddle"/>
        </c:when>
        <c:otherwise>
            <a href= "folder.do?folder=<%=folderName%>&amp;sort=<c:out value="subDN"/> " title="Sort by Subject" >Subject</a> 
        </c:otherwise>
        </c:choose>
   </c:when>
   <c:when test="${column == 'to'}">
        <c:choose>
        <c:when test="${folderSort == 'toUP'}">
            <a href= "folder.do?folder=<%=folderName%>&amp;sort=<c:out value="toDN"/> " title="Sort by Receiver" >To</a>       
            <html:img page="/sort-up.gif" border="0" align="absmiddle"/>
        </c:when>
        <c:when test="${folderSort == 'toDN'}">
            <a href= "folder.do?folder=<%=folderName%>&amp;sort=<c:out value="toUP"/> " title="Sort by Receiver" >To</a>       
            <html:img page="/sort-down.gif" border="0" align="absmiddle"/>
        </c:when>
        <c:otherwise>
            <a href= "folder.do?folder=<%=folderName%>&amp;sort=<c:out value="toDN"/> " title="Sort by Receiver" >To</a> 
        </c:otherwise>
        </c:choose>
   </c:when>
   <c:otherwise>
    <span style="color:red" title="Tell your system admin about this.">BUG!</span>
    <% System.err.println("Unknown column name in messageList.jsp: " + pageContext.getAttribute("column")); %>
   </c:otherwise>
  </c:choose>
 </th>
</c:forEach>
 </tr>

<c:if test="${empty messages}">
 <tr>
  <td colspan="<%= columns.size() + 1 %>" class="folderEmptyFolder">
   <bean:message key="folder.empty"/>
  </td>
 </tr>
</c:if>

<c:forEach items="${messages}" var="message" varStatus="messageStatus">
<%
    Message message = (Message)pageContext.getAttribute("message");
    MessageWrapper wrappedMessage = new MessageWrapper(message);
    pageContext.setAttribute("wrappedMessage", wrappedMessage);
    

    Map messageParams = new HashMap();
    Util.addMessageParams(message,sort, messageParams);
    pageContext.setAttribute("messageParams", messageParams);

    Integer messageNumber = (Integer)messageParams.get("messageNumber"); // For Messages
    Long uid = (Long)messageParams.get("uid"); // For IMAP Messages
    //String messageSort = (string)messageParams.get("sort");

    // Get the Message-ID so it can be added to the TR.
    String messageID = "";
    if (message instanceof MimeMessage && ((MimeMessage)message).getMessageID() != null) {
        //messageID = ResponseUtils.filter(((MimeMessage)message).getMessageID());
        messageID = Integer.toString(((MimeMessage)message).getMessageID().hashCode());
    } else if (message.getFolder() instanceof UIDFolder) {
        // Just in case
        messageID = Long.toString(((UIDFolder)message.getFolder()).getUID(message));
    }
    final String id = messageID != null ? "id=\"" + messageID + "\"" : "";

// XXX: This could be done better with taglibs
if (!message.isSet(Flags.Flag.SEEN)) {
    %>
 <tr class="messageUnread" <%= id %> onMouseOver="st(this)" onMouseOut="ht(this)">
    <%
} else {
    %>
 <tr class="messageSeen" <%= id %> onMouseOver="st(this)" onMouseOut="ht(this)">
    <%
}
%>
  <td class="msgCheckbox">
   <c:choose>
    <c:when test="${! empty messageParams.uid}">
     <input type="checkbox" name="uid" value="<%= uid.toString() %>" onclick="ts(this); uncheckCheckAll(this.form);"/>
    </c:when>
    <c:otherwise>
     <input type="checkbox" name="messageNumber" value="<%= messageNumber.toString() %>" onclick="ts(this); uncheckCheckAll(this.form);"/>
    </c:otherwise>
   </c:choose>
  </td>

 <c:forEach items="${columns}" var="column" varStatus="columnStatus">
  <c:choose>
   <c:when test="${column == 'date'}">
    <td class="msgDate">
     <bean:write name="message" property="sentDate" filter="false" formatKey="message.date.format"/>
    </td>
   </c:when>

   <c:when test="${column == 'flags'}">
<%
    boolean noFlags = true;
%>
  <td class="msgFlags" align="center"><c:forEach items="${message.flags.systemFlags}" var="flag"><%
      Flags.Flag flag = (Flags.Flag)pageContext.getAttribute("flag");
      if (Flags.Flag.ANSWERED.equals(flag)) {
%>
<html:img page="/replied.gif" altKey="message.flags.answered.alt" titleKey="message.flags.answered.title" border="0" align="absmiddle"/>
<%
          noFlags = false;
      } else if (Flags.Flag.DELETED.equals(flag)) {
%>
<html:img page="/deleted.gif" altKey="message.flags.deleted.alt" titleKey="message.flags.deleted.title" border="0" align="absmiddle"/>
<%
          noFlags = false;
      } else if (Flags.Flag.FLAGGED.equals(flag)) {
%>
<html:img page="/flagged.gif" altKey="message.flags.flagged.alt" titleKey="message.flags.flagged.title" border="0" align="absmiddle"/>
<%
          noFlags = false;
      } else if (Flags.Flag.RECENT.equals(flag)) {
%>
<html:img page="/recent.gif" altKey="message.flags.recent.alt" titleKey="message.flags.recent.title" border="0" align="absmiddle"/>
<%
          noFlags = false;
      } else if (Flags.Flag.DRAFT.equals(flag)) {
          //out.println("Draft");
      } else if (Flags.Flag.SEEN.equals(flag)) {
          //out.println("Seen");
      } else if (Flags.Flag.USER.equals(flag)) {
          //out.println("User");
      }
%></c:forEach><%
    if (Util.hasAttachment(message)) {
%>
<html:img page="/attachment.gif" altKey="message.flags.attachment.alt" titleKey="message.flags.attachment.title" border="0" align="absmiddle"/>
<%
        noFlags = false;
    }

    if (junkThreshold > 0) {
        String[] junkHeadersArray = message.getHeader("X-Spam-Level");
        if (junkHeadersArray != null) {
            final Iterator junkHeaders = Arrays.asList(junkHeadersArray).iterator();
            while (junkHeaders.hasNext()) {
                final String junkHeader = (String) junkHeaders.next();
                if (junkHeader != null && junkHeader.indexOf(junkPattern) >= 0) {
%>
<html:img page="/junk.gif" altKey="message.flags.junk.alt" titleKey="message.flags.junk.title" border="0" align="absmiddle"/>
<%
                    noFlags = false;
                    break;
                }
            }
        }
    }

    if (noFlags) {
        out.print("&nbsp;");
    }
%></td>
 <%--
   <c:forEach items="${message.flags.userFlags}" var="flag">
    <bean:write name="flag"/>
   </c:forEach>
--%>
   </c:when>

   <c:when test="${column == 'from'}">
    <td class="msgFrom">
     <c:choose>
      <c:when test="${fn:length(wrappedMessage.from) gt 1}">
       <span class="fromList" title="<c:out value="${wrappedMessage.from}"/>">(<c:out value="${fn:length(wrappedMessage.from)}"/> senders)</span>
      </c:when>
      <c:otherwise>
       <c:forEach items="${wrappedMessage.from}" var="from">
        <wm:write name="from" filter="false" formatKey="folder.view.email.format" formatClassKey="message.email.formatter"/>
       </c:forEach>
      </c:otherwise>
     </c:choose>
    </td>
   </c:when>

   <c:when test="${column == 'size'}">
    <td class="msgSize" align="center">
     <wm:write name="message" property="size" filter="false" formatKey="message.size.format" formatClassKey="message.size.formatter"/>
    </td>
   </c:when>

   <c:when test="${column == 'subject'}">
    <td class="msgSubject">
     <html:link forward="message" name="messageParams">
      <c:choose>
       <c:when test="${! empty message.subject}">
        <wm:write name="message" property="subject" filter="false" formatKey="message.subject.format" formatClassKey="message.subject.formatter"/>
       </c:when>
       <c:otherwise>
        <i class="noSubject"><bean:message key="message.nosubject"/></i>
       </c:otherwise>
      </c:choose>
     </html:link>
    </td>
   </c:when>

   <c:when test="${column == 'to'}">
    <td class="msgTo">
     <c:choose>
      <c:when test="${fn:length(wrappedMessage.to) gt 1}">
       <span class="recipientList" title="<c:out value="${wrappedMessage.to}"/>">(<c:out value="${fn:length(wrappedMessage.to)}"/> recipients)</span>
      </c:when>
      <c:otherwise>
       <c:forEach items="${wrappedMessage.to}" var="from">
        <wm:write name="from" filter="false" formatKey="folder.view.email.format" formatClassKey="message.email.formatter"/>
       </c:forEach>
      </c:otherwise>
     </c:choose>
    </td>
   </c:when>

   <c:otherwise>
    <span style="color:red" title="Tell your system admin about this.">BUG!</span>
    <% System.err.println("Unknown column name in messageList.jsp: " + pageContext.getAttribute("column")); %>
   </c:otherwise>
  </c:choose>
 </c:forEach>

 </tr>
<script language="JavaScript" type="text/javascript"><!--
var msg = new Array();
<%
    String threadSubject = message.getSubject();
    if (threadSubject != null) {
        threadSubject = threadSubject.trim();
        boolean done = false;
        while (!done) {
            final String threadSubjectLC = threadSubject.toLowerCase();
            if (threadSubjectLC.startsWith("re:") || threadSubjectLC.startsWith("fw:")) {
                threadSubject = threadSubject.substring(3).trim();
            } else if(threadSubjectLC.startsWith("fwd:")) {
                threadSubject = threadSubject.substring(4).trim();
            } else if (threadSubjectLC.startsWith("[fwd:") && threadSubject.endsWith("]")) {
                threadSubject = threadSubject.substring(5, threadSubject.length()-1).trim();
            } else if (threadSubjectLC.endsWith("(fwd)")) {
                threadSubject = threadSubject.substring(0, threadSubject.length()-5).trim();
            } else {
                done = true;
            }
        }

%>msg.id = '<%= messageID %>';
msg.threadSubject = '<%= ResponseUtils.filter(threadSubject) %>';
<%
    }

    // Don't fetch headers needed for theading if threading isn't enabled.
    if (threadingEnabled) {
        final Set references = new HashSet();
        String[] headers = message.getHeader("In-Reply-To");
        if (headers != null) {
            for (int i=0; i < headers.length; i++) {
                final StringTokenizer st = new StringTokenizer(headers[i]);
                while (st.hasMoreTokens()) {
                    references.add(st.nextToken());
                }
            }
        }
        headers = message.getHeader("References");
        if (headers != null) {
            for (int i = 0; i < headers.length; i++) {
                final StringTokenizer st = new StringTokenizer(headers[i]);
                while (st.hasMoreTokens()) {
                    references.add(st.nextToken());
                }
            }
        }

        Iterator iter = references.iterator();
        while (iter.hasNext()) {
            //ResponseUtils.filter((String)iter.next())
%>msg[msg.length] = '<%= (iter.next()).hashCode() %>';
<%
        }
    }
%>
msg.eRC = msg.length; // Explicit Reference Count
messageThreads["<%= messageID %>"] = msg;
//--></script>
</c:forEach>

<c:if test="${! empty messages}">
 <tr>
  <td></td>
  <td colspan="<%= columns.size() - 3 %>">
   <a href="JavaScript:checkAll(document.modifyFolderForm.checkAllBox, true)"><bean:message key="link.check.all"/></a>
   /
   <a href="JavaScript:checkAll(document.modifyFolderForm.checkAllBox, false)"><bean:message key="link.check.none"/></a>
  </td>
  <td colspan="3" align="right">
<c:if test="${not empty folderListPage}">
<form>
  <c:choose>
   <c:when test="${folderListPage > 1}">
     <a href="folder.do?folder=<%=folderName%>&amp;sort=<c:out value="${folderSort}"/>&amp;page=<c:out value="${folderListPage-1}"/>">&lt;&nbsp;prev</a>
   </c:when>
   <c:otherwise>
     <a href="folder.do?folder=<%=folderName%>&amp;sort=<c:out value="${folderSort}"/>&amp;page=0">show&nbsp;all</a>
   </c:otherwise>
  </c:choose>
<select onchange="jumpToPage(this)">
<c:forEach begin="1" end="${folderListPages}" var="page">
<c:choose>
 <c:when test="${page == (folderListPage)}">
 <option value="<c:out value="${page}"/>" selected="selected"><c:out value="${page}"/></option>
 </c:when>
 <c:otherwise>
  <option value="<c:out value="${page}"/>"><c:out value="${page}"/></option>
 </c:otherwise>
</c:choose>
</c:forEach>
</select> of <b><c:out value="${folderListPages}"/></b>
<c:if test="${folderListPage < folderListPages}">
   <a href="folder.do?folder=<%=folderName%>&amp;sort=<c:out value="${folderSort}"/>&amp;page=<c:out value="${folderListPage+1}"/>">next&nbsp;&gt;</a>
</c:if>
</form>
</c:if>
  </td>
 </tr>
</c:if>
</table>
</html:form>
<%
    Util.releaseFolder(folder);
%>
<script language="JavaScript" type="text/javascript"><!--

function alreadyRefs(msg, ref) {
    if (msg && ref) {
        var refId;
        // Resolve ref if needed.
        if (typeof ref == "object") {
            refId = ref.id;
        } else {
            refId = ref;
        }
        for (var i=0; i < msg.length; i++) {
            var node = msg[i];
            if (typeof node == "object") node = node.id; // Resolve if it has been optimized
            if (node == ref) return true;
        }
    }
    return false;
}

function crossReferenceSubjects() {
    var subjT = new Object();
    // Iter thought messages building a subject hash
    for (var msgId in messageThreads) {
        var msg = messageThreads[msgId]; // current message
        var subj = msg.threadSubject; // msg's thread based on subject
        if (subj) { // Don't thread the empty subject
            var subjA = subjT[subj]; // the hash for this subject
            if (!subjA) { // Create the new hash if needed.
                subjA = new Object();
                subjT[subj] = subjA;
            }
            subjA[msgId] = msg; // Add the msg object to the hash of msgs for this subject
        }
    }

    for (var subj in subjT) {
        var subjA = subjT[subj];
        for (var msgId in subjA) {
            var msg = messageThreads[msgId];
            for (var msgId2 in subjA) {
                if (!alreadyRefs(msg, msgId2)) {
                    msg[msg.length] = msgId2;
                }
            }
        }
    }
}
crossReferenceSubjects();


function XcrossReference() {
    for (var msgId in messageThreads) {
        var msg = messageThreads[msgId]; // current message
        //var msgLen = msg.length;
        var msgLen = msg.eRC;
        for (var i=0; i < msgLen; i++) {
            var msgId2 = msg[i];
            if (typeof msgId2 == "object") msgId2 = msgId2.id; // Resolve if it has been optimized
            var msg2 = messageThreads[msgId2];
            if (msg2 && !alreadyRefs(msg2, msgId2)) {
                msg2[msg2.length] = msgId;
            }
        }
    }
}
//crossReference();

function XcrossReferenceAllMessages() {
    for (var msgId in messageThreads) {
        var msg = messageThreads[msgId]; // current message
        crossReference(msg, msgId);
    }
}

function crossReferenceAllMessages() {
    for (var msgId in messageThreads) {
        var msg = messageThreads[msgId]; // current message

        var msgLen = msg.eRC;
        for (var i=0; i < msgLen; i++) {
            var msg2 = msg[i];
            if (msg2) crossReference(msg, msg2);
        }
    }
}


function crossReference(msg, ref) {
    if (typeof ref == "object")
        ref = ref.id; // Resolve if it has been optimized

    var node = messageThreads[ref];
    if (node) {
        var nodeLen = node.eRC;
        for (var i=0; i < nodeLen; i++) {
            var msg3ref = node[i];
            if (typeof msg3ref == "object")
                msg3ref = msg3ref.id; // Resolve if it has been optimized

            if (!alreadyRefs(msg, msg3ref)) {
                msg[msg.length] = msg3ref;
            }

            var msg3 = messageThreads[msg3ref];
            if (msg3) {
                if (!alreadyRefs(msg3, msg.id)) {
                    msg3[msg.length] = msg.id;
                }
            }
        }
    }
    window.status = 'd1:' + d1 + ' d2:' + d2 + ' d3:' + d3;
}

//crossReferenceAllMessages();

//--></script>