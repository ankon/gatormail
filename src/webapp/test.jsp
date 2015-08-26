<%@ page import="java.text.MessageFormat,
                 java.util.Date,
                 java.io.IOException,
                 java.io.BufferedReader,
                 java.io.StringReader,
                 org.apache.oro.text.perl.Perl5Util,
                 java.util.Collection,
                 java.util.ArrayList,
                 java.util.List,
                 java.util.Iterator,
                 java.util.Enumeration,
                 javax.naming.Context,
                 javax.naming.InitialContext,
                 edu.ufl.osg.managesieve.PlainSaslMechanism"%>
<%@page contentType="text/html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<html>
<head>
 <title>Test page</title>
 <style type="text/css">
    ul {
        border : solid;
        padding-right : 1em;
    }
 </style>
</head>
<body>

<hr/>

<ul>
<%= listHeaders(request) %>
</ul>
<hr/>
<%
    String lines = "This is a test. This is only a test.\n";
    lines += "This is a test. This is only a test. in the case of a real test this osdg adfad fa dfgad ad ad \n";
%>
<pre>
         1         2         3         4         5         6         7         8
12345678901234567890123456789012345678901234567890123456789012345678901234567890
<%= wrapLines(lines) %></pre>
</body>
</html>
<%!
    private static String listHeaders(HttpServletRequest request) {
        String out = "";

        Enumeration hEnum = request.getHeaderNames();
        while (hEnum.hasMoreElements()) {
            String headerName = (String) hEnum.nextElement();
            Enumeration hEnum2 = request.getHeaders(headerName);
            while (hEnum2.hasMoreElements()) {
                out += "<li>" + headerName + ": ";
                out += hEnum2.nextElement();
                out += "</li>\n";
            }
        }
        return out;
    }

    private static String wrapLines(String body) throws IOException {
        StringBuffer buff = new StringBuffer(body.length());
        BufferedReader br = new BufferedReader(new StringReader(body));
        String line = null;

        while ((line = br.readLine()) != null) {
            if (line.length() < 66) {
                // XXX We should trim trailing spaces for flowed text. see RFC2546
                buff.append(line + "\n");
            } else {
                Perl5Util p5u = new Perl5Util();
                List list = new ArrayList();
                line = p5u.substitute("s/(\\s*$)//g", line); // Trim trailing white space
                buff.append(p5u.substitute("s/(.{1,66}(?:\\s*$|\\s))/$1\n/g", line));

            }
        }

        return buff.toString();
    }

    private static String wrapLines2(String body) throws IOException {
        StringBuffer buff = new StringBuffer(body.length());
        BufferedReader br = new BufferedReader(new StringReader(body));
        String line = null;

        while ((line = br.readLine()) != null) {
            if (line.length() < 66) {
                // XXX We should trim trailing spaces for flowed text. see RFC2546
                buff.append(line + "\n");
            } else {
                Perl5Util p5u = new Perl5Util();
                List list = new ArrayList();
                p5u.split(list, "/(.{1,26})\\s/", line);
                //System.err.println("list.size: " + list.size());
                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    String s = (String)iter.next();
                    if (s.length() != 0) {
                        buff.append(s + " \n");
                    }
                }
            }
        }

        return buff.toString();
    }
%>