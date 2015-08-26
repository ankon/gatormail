<%@page contentType="text/html" import="org.apache.struts.action.ActionErrors,
                                        java.util.Enumeration"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>

<h1>Houston, we have a problem!</h1>

<p>
 GatorMail is unable to track that you are logged in unless cookies have been
 enabled and it appears that cookies are not enabled in your web browser.
</p>

<p>
 Please check that cookies are enabled and try to login again. If you are unsure
 how to enable cookies either refer to your brower's help or you can follow the
 directions from
 <html:link href="http://www.google.com/cookies.html">Google's How to Enable Cookies</html:link>
 page.
</p>

<p>
 <html:link forward="login">Back to the login page</html:link>
</p>

<%--
<h2>Houston, we think we know how to fix this.</h2>

<p>
 Due to the feedback from a user we know what the problem is for those of you
 who do have cookies enabled and are still having problems. We have a fix
 ready but it won't be made active until we restart GatorMail late tonight
 (2/25/2003) so that we do not interrupt any users currently logged in.
</p>

<p>
 Until we have enabled the fix you can work around this problem by closing all
 browser windows to make sure the JSESSIONID cookie has been flushed from your
 browser. Then visit
 <html:link href="https://test.webmail.ufl.edu/">https://test.webmail.ufl.edu/</html:link>
 directly by typing it in your address bar. You can still browse other websites,
 it's just important that you do not browse
 <html:link href="https://webmail.ufl.edu/">https://webmail.ufl.edu/</html:link>
 while logged in to GatorMail until we can implement the fix tonight.
</p>
--%>

<p>
 If you have verified that cookies are enabled and this does not solve your
 problem, please email us at open-systems-l@lists.ufl.edu with a description
 of anything you think may be relevant. Please copy/paste the information below
 into your email, as it will aid us in resolving this problem.
</p>

<textarea rows="15" cols="80"><%
    Enumeration hEnum = request.getHeaderNames();
    while (hEnum.hasMoreElements()) {
        String header = (String)hEnum.nextElement();
        Enumeration hEnum2 = request.getHeaders(header);
        while (hEnum2.hasMoreElements()) {
            out.println(header + ": " + hEnum2.nextElement());
        }
    }
%></textarea>
