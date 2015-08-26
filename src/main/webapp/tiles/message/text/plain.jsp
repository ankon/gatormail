<%@page contentType="text/html" import="org.apache.struts.util.ResponseUtils,
                                        java.io.BufferedReader,
                                        java.io.StringReader,
                                        java.util.List,
                                        java.util.ArrayList,
                                        java.io.StringWriter,
                                        javax.mail.BodyPart,
                                        javax.mail.Message,
                                        java.io.IOException,
                                        org.apache.oro.text.perl.Perl5Util,
                                        org.apache.oro.text.PatternCacheLRU"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<table class="messageBody" cellspacing="0" cellpadding="2" width="100%" border="0">
 <tr>
  <th class="leftNavBar" width="100">&nbsp;</th>
  <td class="messageTextPlain">
<%
    try {
        // Get this part.
        Object o = request.getAttribute("body");
        BodyPart bodyPart = null;
        String message = null;
        boolean isFlowed = false;

        if (o instanceof BodyPart) {
            bodyPart = (BodyPart)o;
            message = (String)bodyPart.getContent();
        } else {
            message = (String)o;
        }

        // Is this this message flowed?
        String contentType;
        if (bodyPart != null) {
            contentType = bodyPart.getContentType().toLowerCase();
        } else {
            // If there are no mime parts get the info from the message.
            Message m = (Message)request.getAttribute("message");
            contentType = m.getContentType().toLowerCase();
        }
        isFlowed = contentType.indexOf("format=flowed") != -1 || contentType.indexOf("format=\"flowed\"") != -1;

        if (isFlowed) {
            formatFlowed(message, pageContext);
        } else {
            formatNormal(message, pageContext);
        }

%>
<bean:write name="newBody" filter="false"/>
<%
    } catch (java.io.UnsupportedEncodingException uee) {
%>
    <i class="UnsupportedEncodingException"><bean:message key="message.error.unsupported.encoding" arg0="<%= uee.getMessage() %>"/></i>
    <tiles:insert name="message"/>
<%
    }
%>
  </td>
 </tr>
</table>
<%!

    private void formatNormal(String message, PageContext pageContext) throws IOException {
        // Filter, wrap, and escape the message
        message = ResponseUtils.filter(message);


        final Perl5Util p5u = new Perl5Util(new PatternCacheLRU(3));
        final StringBuffer sb = new StringBuffer(message.length());

        // Expand Tabs
        p5u.substitute(sb, "s/\t/&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/gm", message);
        message = sb.toString();
        sb.setLength(0);

        // Keep spaces
        p5u.substitute(sb, "s/  / &nbsp;/gm", message);
        message = sb.toString();
        sb.setLength(0);

        // Add breaks at EOL.
        p5u.substitute(sb, "s/$/<br\\/>/gm", message);
        message = sb.toString();
        sb.setLength(0);

        p5u.substitute(sb, URLIFY_REGEX, message);
        message = sb.toString();
        sb.setLength(0);

        BufferedReader br = new BufferedReader(new StringReader(message));
        List lines = new ArrayList();
        String line;

        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
        br.close();
        br = null;

        List linesPre = new ArrayList(lines.size());
        for (int i=0; i < lines.size(); i++) {
            linesPre.add("");
        }

        // TODO: Make this O(n)
        boolean noMoreBlocks = false;
        while (!noMoreBlocks) {
            noMoreBlocks = true;

            boolean inBlock = false;
            for (int i=0; i < lines.size(); i++) {
                line = (String)lines.get(i);
                if (inBlock) {
                    if (line.startsWith("&gt; ")) {
                        lines.set(i, line.substring(5));
                    } else if (line.startsWith("&gt;")) {
                        lines.set(i, line.substring(4));
                    } else {
                        inBlock = false;
                        lines.set(i, (String)lines.get(i) + "</div>");
                    }
                } else {
                    if (line.startsWith("&gt; ")) {
                        inBlock = true;
                        noMoreBlocks = false;
                        linesPre.set(i, (String)linesPre.get(i) + "<div class=\"messageInlineForwardBlock\">");
                        lines.set(i, line.substring(5));
                    } else if (line.startsWith("&gt;")) {
                        inBlock = true;
                        noMoreBlocks = false;
                        linesPre.set(i, (String)linesPre.get(i) + "<div class=\"messageInlineForwardBlock\">");
                        lines.set(i, line.substring(4));
                    } else {
                        // Nothing
                    }
                }
            }

        } // for times

        StringWriter sw = new StringWriter();
        //sw.write("<pre>");
        for (int i=0; i < lines.size(); i++) {
            sw.write((String)linesPre.get(i) + (String)lines.get(i) + "\n");
        }
        //sw.write("</pre>");

        pageContext.setAttribute("newBody", sw.toString());
    }

    // TODO: Put the Perl5Util in a ThreadLocal thingy
    private static final String BOLD_REGEX = "s/(\\W)(\\*.*?\\*)(\\W)/$1<b>$2<\\/b>$3/gm";
    private static final String ITALIC_REGEX = "s/(\\W)(\\/.*?\\/)(\\W)/$1<i>$2<\\/i>$3/gm";
    private static final String UNDERLINE_REGEX = "s/(\\W)(\\_.*?\\_)(\\W)/$1<u>$2<\\/u>$3/gm";

    private static final String URLIFY_REGEX =
            //"s!(http://(?:[a-zA-Z0-9/%?=_@~$`()|+*#\\.,;:-]|\\[|\\]|&amp;)*)!<a href=\"$1\">$1</a>!g";
            "s!((?:about|ftp|http|https|gopher|mailto|mailbox|news|snews|nntp|rlogin|telnet|tn3270|wais|file|prospero|urn|ed2k):(?:[a-zA-Z0-9/%?=_@~$`()|+*#\\.,;:-]|\\[|\\]|&amp;)*)!<a href=\"$1\">$1</a>!g";


    /**
     * http://www.ietf.org/rfc/rfc2646.txt
     */
    private void formatFlowed(String message, PageContext pageContext) throws IOException {
        StringBuffer sb = new StringBuffer(message.length());


        {
            message = ResponseUtils.filter(message);

            Perl5Util p5u = new Perl5Util(new PatternCacheLRU(3));

            // Expand Tabs
            p5u.substitute(sb, "s/\t/&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;/gm", message);
            message = sb.toString();
            sb.setLength(0);

            // Keep spaces
            p5u.substitute(sb, "s/  / &nbsp;/gm", message);
            message = sb.toString();
            sb.setLength(0);

            p5u.substitute(sb, BOLD_REGEX, message);
            message = sb.toString();
            sb.setLength(0);

            // TODO Causes problems with the / in urls
            //p5u.substitute(sb, ITALIC_REGEX, message);
            //message = sb.toString();
            //sb.setLength(0);

            p5u.substitute(sb, UNDERLINE_REGEX, message);
            message = sb.toString();
            sb.setLength(0);

            p5u.substitute(sb, URLIFY_REGEX, message);
            message = sb.toString();
            sb.setLength(0);

        }

        {
            BufferedReader br = new BufferedReader(new StringReader(message));
            String line;

            sb.append("<p>");
            int previousQuoteDepth = 0;
            int quoteDepth = 0;
            while ((line = br.readLine()) != null) {
                quoteDepth = countEscapedQuoteDepth(line);

                // Block quote:
                if (previousQuoteDepth < quoteDepth) {
                    for (int i=previousQuoteDepth; i < quoteDepth; i++) {
                        sb.append("<div class=\"messageInlineForwardBlock\">");
                    }
                } else if (previousQuoteDepth > quoteDepth) {
                    for (int i=previousQuoteDepth; i > quoteDepth; i--) {
                        sb.append("</div>");
                    }
                }

                // Trim leading block '>'
                line = line.substring(quoteDepth * 4);

                // Eat stuff space
                if (line.startsWith(" ")) {
                    line = line.substring(1);
                }

                boolean hardBreak = true;
                if (line.endsWith(" ") && !line.endsWith("-- ")) {
                    hardBreak = false;
                }

                if (hardBreak) {
                    // Hard break
                    sb.append(line);
                    sb.append("<br/>");
                    sb.append("\n");
                } else {
                    // Soft break
                    sb.append(line);
                    sb.append("\n");
                }
                previousQuoteDepth = quoteDepth;
            }
            sb.append("</p>");
            br.close();
        }

        pageContext.setAttribute("newBody", sb.toString());
    }

    /**
     * Counts the number of '&gt;' at the start of a line.
     *
     * @param  line                a string
     * @return                     the number of '&gt;' at the start of a line
     */
    public static int countEscapedQuoteDepth(final String line) {
        final int length = line.length();
        int depth;

        for (depth = 0; depth < length && line.startsWith("&gt;",depth * 4); depth++) ;

        return depth;
    }

%>