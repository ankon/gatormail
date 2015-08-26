<%@ page import="java.text.StringCharacterIterator,
                 javax.mail.BodyPart,
                 java.io.InputStream,
                 java.io.InputStreamReader,
                 java.io.StringWriter,
                 java.text.CharacterIterator"%>
<%@page contentType="text/html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<table class="messageBody" cellspacing="0" cellpadding="2" width="100%" border="0">
 <tr>
  <th class="leftNavBar" width="100">&nbsp;</th>
  <td class="messageTextEnriched">
<%
    // Get this part.
    Object o = request.getAttribute("body");
    String message = null;
    try {
        if (o instanceof BodyPart) {
            BodyPart bodyPart = (BodyPart)o;
            o = bodyPart.getContent();
        }
        if (o instanceof InputStream) {
            InputStream is = (InputStream)o;
            message = " "; // XXX: Why is this needed?
            StringBuffer sb = new StringBuffer(1024);
            byte[] buf = new byte[1024];
            int sizeRead = 0;
            while ((sizeRead = is.read(buf)) > 0) {
                String s = new String(buf, 0, sizeRead);
                sb.append(s);
            }
            message += sb.toString();
        } else {
            message = (String)o;
        }

        message = translate(message);
    } catch (Throwable t) {
        t.printStackTrace(System.err);
    }
    pageContext.setAttribute("newBody", message);
%>
   <bean:write name="newBody" filter="false"/>
  </td>
 </tr>
</table>
<%!
    static String translate(String body) {

        char c;
        boolean consecNewLineBit = false;
        boolean consecLessThanBit = false;
        boolean isToken = false;
        String token = "";
        StringBuffer sb = new StringBuffer();
        StringCharacterIterator iter;

        try {

            iter = new StringCharacterIterator(body);

            // Read a new character
            while ( (c = iter.next()) != CharacterIterator.DONE) {

                // Make a string out of it
                String theChar = String.valueOf(c);

                // If it's not a "<"...
                //if ( !(theChar.equals("<"))) {
                if (c != '<') {

                    // (reset the consecutive less-than bit)
                    consecLessThanBit = false;

                    // ...and it's not a newline...
                    //if (!(theChar.equals("\n"))) {
                    if (!(c == '\n' || c == '\r')) {

                        // (reset the consecutive newline bit)
                        consecNewLineBit = false;

                        // ...and it's not part of a token...
                        if (!isToken) {

                            // ...and it's not a greater-than...
                            if (c != '>') {
                                // ...then just write it out.
                                sb.append(c);
                            }

                            // But if it *is* a greater-than...
                            else {
                                // print out a gt entity.
                                sb.append("&gt;");
                            }
                        }

                        // But if it *is* part of a token...
                        else {

                            // ...and it's not the *end* of a token...
                            if (c != '>') {
                                // ...add it to the token String.
                                token = token + theChar;
                            }

                            // But if it *is* the end of the token...
                            else {
                                // ... parse the token and clean up.
                                sb = parseToken(token, sb);
                                isToken = false;
                                token = "";
                            }
                        }
                    }

                    // But if it *is* a newline...
                    else {

                        // ...and it's not the first...
                        if (consecNewLineBit) {
                            // print a <br />.
                            sb.append("<br />\n");
                        }

                        // ...but if it *is* the first...
                        else {
                            // print a space and set the
                            // consecNewLineBit so if the next
                            // character is a new line also, it
                            // gets treated correctly.
                            sb.append(" ");
                            consecNewLineBit = true;
                        }
                    }

                }

                // But if it *is* a '<'...
                else {

                    consecNewLineBit = false;

                    // ... and it's not the first...
                    if (consecLessThanBit) {
                        // print a literal less-than and reset the bit.
                        // Also turn off the token bit, this wasn't
                        // the start of a token.
                        sb.append("&lt;");
                        consecLessThanBit = false;
                        isToken = false;
                    }

                    // But if it *is* the first...
                    else {

                        // Set the consecutive less-than bit, so
                        // if the next character is a less-than also,
                        // a literal one will be printed.
                        consecLessThanBit = true;

                        // Set the 'this is a token' bit so if the
                        // next character isn't a less-than, it gets
                        // tokenized.
                        isToken = true;
                    }
                }
            }
        }

        catch(Exception e) {
            e.printStackTrace(System.err);
        }

        return sb.toString();

    }

    static StringBuffer parseToken(String token, StringBuffer sb) {

        // If the token is "param"...
        if (token.equalsIgnoreCase("param")) {

            // print it out
            sb.append("<param ");
        }

        else if (token.equalsIgnoreCase("/param")) {

            // print it out
            sb.append("> ");
        }

        else if (token.equalsIgnoreCase("bold")) {
            sb.append("<b>");
        }

        else if (token.equalsIgnoreCase("/bold")) {
            sb.append("</b>");
        }

        else if (token.equalsIgnoreCase("italics")) {
            sb.append("<i>");
        }

        else if (token.equalsIgnoreCase("/italics")) {
            sb.append("</i>");
        }

        else if (token.equalsIgnoreCase("excerpt")) {
            sb.append("<blockquote>");
        }

        else if (token.equalsIgnoreCase("/excerpt")) {
            sb.append("</blockquote");
        }

        else if (token.equalsIgnoreCase("nofill")) {
            sb.append("<pre>");
        }

        else if (token.equalsIgnoreCase("/nofill")) {
            sb.append("</pre>");
        }

        else if (token.equalsIgnoreCase("fixed")) {
            sb.append("<tt>");
        }

        else if (token.equalsIgnoreCase("/fixed")) {
            sb.append("</tt>");
        }

        else if (token.equalsIgnoreCase("center")) {
            sb.append("<center>");
        }

        else if (token.equalsIgnoreCase("/center")) {
            sb.append("</center>");
        }

        else if (token.equalsIgnoreCase("flushright")) {
            sb.append("<div style='text-align : right'>");
        }

        else if (token.equalsIgnoreCase("/flushright")) {
            sb.append("</div>");
        }

        else if (token.equalsIgnoreCase("flushleft")) {
            sb.append("<div style='text-align : left'>");
        }

        else if (token.equalsIgnoreCase("/flushleft")) {
            sb.append("</div>");
        }

        else if (token.equalsIgnoreCase("flushboth")) {
            sb.append("<div style='text-align : justify'>");
        }

        else if (token.equalsIgnoreCase("/flushboth")) {
            sb.append("</div>");
        }

        else {
            sb.append("<?").append(token).append(">");
        }

        return sb;

    }

%>