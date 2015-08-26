<%@ page import="javax.mail.BodyPart,
                 java.io.InputStream,
                 java.text.StringCharacterIterator,
                 java.text.CharacterIterator"%>
<%@page contentType="text/html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<table class="messageBody" cellspacing="0" cellpadding="2" width="100%" border="0">
 <tr>
  <th class="leftNavBar" width="100">&nbsp;</th>
  <td class="messageTextRichtext">
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
                                    //sb.append("<br />\n");
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

                else if (token.equalsIgnoreCase("italic")) {
                    sb.append("<i>");
                }

                else if (token.equalsIgnoreCase("/italic")) {
                    sb.append("</i>");
                }

                else if (token.equalsIgnoreCase("fixed")) {
                    sb.append("<tt>");
                }

                else if (token.equalsIgnoreCase("/fixed")) {
                    sb.append("</tt>");
                }

                else if (token.equalsIgnoreCase("smaller")) {
                    sb.append("<small>");
                }

                else if (token.equalsIgnoreCase("/smaller")) {
                    sb.append("</small");
                }

                else if (token.equalsIgnoreCase("bigger")) {
                    sb.append("<big>");
                }

                else if (token.equalsIgnoreCase("/bigger")) {
                    sb.append("</big>");
                }

                // This is deprecated in HTML 4.0, should it be removed?
                else if (token.equalsIgnoreCase("underline")) {
                    sb.append("<u>");
                }

                else if (token.equalsIgnoreCase("/underline")) {
                    sb.append("</u>");
                }

                // This is deprecated in HTML 4.0, should it be removed?
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
                // Not sure what to do here
                else if (token.equalsIgnoreCase("indent")) {
                    sb.append("<div class='richtext-indent' style='padding-left : 3em'>");
                }

                else if (token.equalsIgnoreCase("/indent")) {
                    sb.append("</div>");
                }

                // Not sure what to do here
                else if (token.equalsIgnoreCase("indentright")) {
                    sb.append("<div class='richtext-indentright' style='padding-right : 3em'>");
                }

                else if (token.equalsIgnoreCase("/indentright")) {
                    sb.append("</div>");
                 }

                // Not sure what to do here
                else if (token.equalsIgnoreCase("outdent")) {
                    sb.append("<div class='richtext-outdent'>");
                }

                else if (token.equalsIgnoreCase("/outdent")) {
                    sb.append("</div>");
                }

                // Not sure what to do here
                else if (token.equalsIgnoreCase("outdentright")) {
                    sb.append("<div class='richtext-outdentright'>");
                }

                else if (token.equalsIgnoreCase("/outdentright")) {
                    sb.append("</div>");
                }

                // Not sure what to do here
                else if (token.equalsIgnoreCase("samepage")) {
                    sb.append("<div class='richtext-samepage'>");
                }

                else if (token.equalsIgnoreCase("/samepage")) {
                    sb.append("</div>");
                }

                else if (token.equalsIgnoreCase("subscript")) {
                    sb.append("<sub class='richtext-subscript'>");
                }

                else if (token.equalsIgnoreCase("/subscript")) {
                    sb.append("</sub>");
                }

                else if (token.equalsIgnoreCase("superscript")) {
                    sb.append("<sup class='richtext-superscript'>");
                }

                else if (token.equalsIgnoreCase("/superscript")) {
                    sb.append("</sup>");
                }

                else if (token.equalsIgnoreCase("heading")) {
                    sb.append("<div class='richtext-heading'>");
                }

                else if (token.equalsIgnoreCase("/heading")) {
                    sb.append("</div>");
                }

                else if (token.equalsIgnoreCase("footing")) {
                    sb.append("<div class='richtext-footing'>");
                }

                else if (token.equalsIgnoreCase("/footing")) {
                    sb.append("</div>");
                }

                else if (token.equalsIgnoreCase("excerpt")) {
                    sb.append("<div class='richtext-excerpt messageInlineForwardBlock'>");
                }

                else if (token.equalsIgnoreCase("/excerpt")) {
                    sb.append("</div>");
                }

                else if (token.equalsIgnoreCase("paragraph")) {
                    sb.append("<p class='richtext-paragraph'>");
                }

                else if (token.equalsIgnoreCase("/paragraph")) {
                    sb.append("</p>");
                }

                else if (token.equalsIgnoreCase("signature")) {
                    sb.append("<div class='richtext-signature'>");
                }

                else if (token.equalsIgnoreCase("/signature")) {
                    sb.append("</div>");
                }

                else if (token.equalsIgnoreCase("comment")) {
                    sb.append("<!-- <div class='richtext-comment'>");
                }

                else if (token.equalsIgnoreCase("/comment")) {
                    sb.append("</div> -->");
                }

                else if (token.equalsIgnoreCase("no-op")) {
                    sb.append("<span class='richtext-no-op'>");
                }

                else if (token.equalsIgnoreCase("/no-op")) {
                    sb.append("</span>");
                }

                else if (token.equalsIgnoreCase("lt")) {
                    sb.append("&lt;");
                }

                else if (token.equalsIgnoreCase("nl")) {
                    sb.append("<br class='richtext-nl'/>");
                }

                else if (token.equalsIgnoreCase("np")) {
                    sb.append("<br class='richtext-np'/>");
                }

                else {
                    sb.append("<?").append(token).append(">");
                }

                return sb;

            }

%>