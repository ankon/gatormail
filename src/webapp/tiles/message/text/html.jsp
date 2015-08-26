<%@page contentType="text/html" import="javax.mail.internet.*,
                                        javax.mail.BodyPart,
                                        org.apache.oro.text.perl.Perl5Util,
                                        org.apache.oro.text.PatternCacheLRU,
                                        edu.ufl.osg.webmail.util.Util,
                                        javax.mail.Folder,
                                        com.sun.mail.imap.IMAPFolder,
                                        javax.mail.Message,
                                        org.apache.struts.util.RequestUtils,
                                        org.apache.struts.util.ResponseUtils,
                                        java.util.List,
                                        java.util.ArrayList,
                                        java.util.Iterator,
                                        javax.xml.transform.dom.DOMSource,
                                        org.w3c.dom.Document,
                                        javax.xml.parsers.DocumentBuilder,
                                        javax.xml.parsers.DocumentBuilderFactory,
                                        javax.xml.transform.Source,
                                        javax.xml.transform.stream.StreamResult,
                                        javax.xml.transform.Transformer,
                                        javax.xml.transform.TransformerFactory,
                                        org.cyberneko.html.parsers.DOMParser,
                                        org.xml.sax.InputSource,
                                        java.io.FileInputStream,
                                        java.io.InputStream,
                                        java.io.OutputStream,
                                        java.io.Reader,
                                        java.io.StringReader,
                                        java.io.Writer,
                                        java.io.StringWriter"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%--
 TODO:
    Alot of stuff in this page is a gross hack. A more accurate solution would
    be to use Neko XNI to build a DOM and filter the DOM and reserilize the
    tree. http://www.apache.org/~andyc/neko/doc/html/
--%>
<%!
    private static Perl5Util p5u = new Perl5Util(new PatternCacheLRU(25));

%>
<%
    Folder folder = (Folder)request.getAttribute("realFolder");
    Message realMessage = (Message)request.getAttribute("realMessage");

    // Get this part.
    Object o = request.getAttribute("body");
    String message = null;
    if (o instanceof BodyPart) {
        BodyPart bodyPart = (BodyPart)o;
        message = (String)bodyPart.getContent();
    } else {
        message = (String)o;
    }

    // TODO Try to use html:rewrite ot something
    String attachmentUrl = request.getContextPath() +  Util.getFromBundle("message.attachment.url");
    attachmentUrl += "?folder=" + ResponseUtils.filter(folder.getFullName());
    if (folder instanceof IMAPFolder) {
        IMAPFolder imapFolder = (IMAPFolder)folder;
        attachmentUrl += "&amp;uid=" + ResponseUtils.filter(Long.toString(imapFolder.getUID(realMessage)));
    } else {
        attachmentUrl += "&amp;messageNumber=" +  ResponseUtils.filter(Integer.toString(realMessage.getMessageNumber()));
    }

    String regex = "s!\"cid:(.*?)\"!\"" + attachmentUrl + "&amp;cid=$1\"!mig";
    message = p5u.substitute(regex, message);

    // Filter JavaScript
    if (true) {
        // Cursive idea inspired from Y!
        regex = "s/<script/<cursive/mig";
        message = p5u.substitute(regex, message);

        regex = "s/<\\/script/<\\/cursive/mig";
        message = p5u.substitute(regex, message);

        regex = "s/(href=.*?)javascript/$1javascriptDISABLED/mig";
        message = p5u.substitute(regex, message);

        List filters = new ArrayList();
        filters.add("onabort");
        filters.add("onblur");
        filters.add("onchange");
        filters.add("onclick");
        filters.add("ondblclick");
        filters.add("ondragdrop");
        filters.add("onerror");
        filters.add("onfocus");
        filters.add("onkeydown");
        filters.add("onkeypress");
        filters.add("onkeyup");
        filters.add("onload");
        filters.add("onmousedown");
        filters.add("onmousemove");
        filters.add("onmouseout");
        filters.add("onmouseover");
        filters.add("onmouseup");
        filters.add("onmove");
        filters.add("onreset");
        filters.add("onresize");
        filters.add("onselect");
        filters.add("onsubmit");
        filters.add("onunload");
        filters.add("onupload");
        Iterator iter = filters.iterator();
        while (iter.hasNext()) {
            String s = (String)iter.next();
            regex = "s/(\\s" + s + ")=/$1DISABLED=/mig";
            message = p5u.substitute(regex, message);
        }
    } else {
        // Presently disabled as it causes problems under load.
        StringWriter sw = new StringWriter(message.length());
        sanitize(new StringReader(message), sw);
        message = sw.toString();
    }

    pageContext.setAttribute("newBody", message);
%>
<!-- message.text/html -->
<table class="messageBody" cellspacing="0" cellpadding="2" width="100%" border="0">
<tr>
 <th class="leftNavBar" width="100">&nbsp;</th>
 <td class="messageTextHtml">
<bean:write name="newBody" filter="false"/>
 </td>
</tr>
</table>
<%!
    public void sanitize(final Reader in, final Writer out) throws Exception {
        final InputSource inSource = new InputSource(in);

        final DOMParser domParser = new DOMParser();


        domParser.setProperty("http://cyberneko.org/html/properties/names/elems", "upper");

        domParser.setProperty("http://cyberneko.org/html/properties/names/attrs", "lower");

        domParser.parse(inSource);

        final Document doc = domParser.getDocument();

        final TransformerFactory transformerFactory = TransformerFactory.newInstance();

        final Source xslSource = loadXSLSource();

        final Transformer transformer = transformerFactory.newTransformer(xslSource);

        transformer.transform(new DOMSource(doc), new StreamResult(out));
    }

    private Source loadXSLSource() throws Exception {
        final String XSL_SOURCE = "/html-sanitize.xsl";

        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        final DocumentBuilder db = dbf.newDocumentBuilder();
        final Document d = db.parse(getClass().getResourceAsStream(XSL_SOURCE));

        //String externalForm = getClass().getResource(getStylesheetName()).toExternalForm();

        //return new DOMSource(d, externalForm);
        return new DOMSource(d);
    }
%>