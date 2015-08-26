<%@page contentType="text/html" import="edu.ufl.osg.webmail.util.Util,
                                        javax.mail.Folder,
                                        com.sun.mail.imap.IMAPFolder,
                                        edu.ufl.osg.webmail.beans.QuotaBean,
                                        com.sun.mail.imap.IMAPStore,
                                        java.util.Arrays,
                                        edu.ufl.osg.webmail.Constants,
                                        edu.ufl.osg.webmail.User,
                                        java.util.List,
                                        java.util.Iterator"%>
<%@ page import="com.opensymphony.oscache.web.ServletCacheAdministrator"%>
<%@ page import="com.opensymphony.oscache.base.Cache"%>
<%@ page import="com.opensymphony.oscache.base.NeedsRefreshException"%>
<%@ page import="com.opensymphony.oscache.base.CacheEntry"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%
    final Folder folder = Util.getFolder(session, "INBOX");
    Util.releaseFolder(folder);
    if (!(folder instanceof IMAPFolder)) {
        out.println("<!-- This folder does not support quotas. -->");
        return;
    }

    final User user = (User)session.getAttribute(Constants.USER_KEY);
    List nameSpaces;
    final ServletCacheAdministrator admin = ServletCacheAdministrator.getInstance(session.getServletContext());
    final Cache cache = admin.getSessionScopeCache(session);
    final String key = "imapStore.getUserNamespaces";
    try {
        nameSpaces = (List)cache.getFromCache(key, CacheEntry.INDEFINITE_EXPIRY);
    } catch (NeedsRefreshException nre) {
        try {
            final IMAPFolder imapFolder = (IMAPFolder)folder;
            final IMAPStore imapStore = (IMAPStore)imapFolder.getStore();
            nameSpaces = Arrays.asList(imapStore.getUserNamespaces(user.getUsername()));
            cache.putInCache(key, nameSpaces, new String[] {"mailStore"});
        } catch (Exception e) {
            nameSpaces = (List)nre.getCacheContent();
            cache.cancelUpdate(key);
        }
    }

    if (request.getAttribute("quotaSet") == null) {
        out.println("<!-- No quota list was supplied. -->");
        return;
    }
%>
<div class="forceIENonStandardsCenter">
  <div class="folderQuota">
    <div class="folderQuotaTitle"><bean:message key="folder.quota.title"/></div>

<c:forEach items="${quotaSet}" var="quotaBean">
<%
    final QuotaBean qb = (QuotaBean)pageContext.findAttribute("quotaBean");
    String quotaRootName = qb.getQuotaRoot();
    final String manualMatch = "user." + user.getUsername();
    if (nameSpaces.size() > 0) {
        final Iterator nsIterator = nameSpaces.iterator();
        while (nsIterator.hasNext()) {
            final Folder f = (Folder)nsIterator.next();
            if (quotaRootName.startsWith(f.getFullName())) {
                quotaRootName = "INBOX" + quotaRootName.substring(f.getFullName().length());
            }
        }
    } else if (quotaRootName.startsWith(manualMatch)) {
        quotaRootName = "INBOX" + quotaRootName.substring(manualMatch.length());
    }
    pageContext.setAttribute("quotaRootName", quotaRootName);
%>
 <c:forEach items="${quotaBean.resources}" var="resource">
      <div class="folderQuotaResource">
        <div class="folderQuotaResourceTitle" title="<c:out value="${quotaBean.quotaRoot}"/>"><c:out value="${quotaRootName}"/></div>
        <div class="folderQuotaResourceBar">
          <div class="folderQuotaResourceUsed" style="width:<c:out value="${resource.percentUsed}"/>%;" title="<c:out value="${resource.name}"/> <c:out value="${resource.percentUsed}"/>% used"></div>
          <div class="folderQuotaResourceFree" style="width:<c:out value="${resource.percentFree}"/>%;" title="<c:out value="${resource.name}"/> <c:out value="${resource.percentFree}"/>% free"></div>
        </div>
        <div class="folderQuotaMessage">
         <bean:message key="folder.quota.message" arg0="<%= Long.toString(((QuotaBean.ResourceBean)pageContext.getAttribute("resource")).getUsage()) %>" arg1="<%= Long.toString(((QuotaBean.ResourceBean)pageContext.getAttribute("resource")).getLimit()) %>"/> 
        </div>
      </div>

 </c:forEach>

</c:forEach>

    </div>
  </div>
</div>