<%@page contentType="text/html"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<html:html locale="true" xhtml="true">
<head>
 <title>
  <bean:message key="site.title"/>
  <tiles:getAsString name="title"/>
 </title>
 <html:base/>
 <link rel="shortcut icon" href="favicon.ico" type="image/x-icon" />
 <link rel="stylesheet" type="text/css" href="<html:rewrite forward="CSS"/>"/>
<%-- // Useful for when we need to force people to connect to a new clone.
    <script>
        function deleteCookie(name, path, domain) {
            if (getCookie(name)) {
                document.cookie = name + "=" +
                                  ((path) ? "; path=" + path : "") +
                                  ((domain) ? "; domain=" + domain : "") +
                                  "; expires=Thu, 01-Jan-70 00:00:01 GMT";
            }
        }
        function getCookie(name) {
            var dc = document.cookie;
            var prefix = name + "=";
            var begin = dc.indexOf("; " + prefix);
            if (begin == -1) {
                begin = dc.indexOf(prefix);
                if (begin != 0) return null;
            } else
                begin += 2;
            var end = document.cookie.indexOf(";", begin);
            if (end == -1)
                end = dc.length;
            return unescape(dc.substring(begin + prefix.length, end));
        }
    </script>
--%>
</head>
<body>

<center>
 <div class="header">
  <tiles:get name="header"/>
 </div>

 <div class="body">
  <tiles:get name="body"/>
 </div>

 <div class="footer">
  <tiles:get name="footer"/>
 </div>
</center>

</body>
</html:html>
