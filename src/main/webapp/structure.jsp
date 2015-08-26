<%@ page import="javax.mail.Folder,
                 edu.ufl.osg.webmail.util.Util,
                 javax.mail.Message,
                 com.sun.mail.imap.IMAPFolder,
                 java.io.IOException,
                 javax.mail.internet.MimeMultipart,
                 javax.mail.MessagingException,
                 javax.mail.internet.MimeBodyPart,
                 java.util.Enumeration,
                 javax.mail.internet.MimeMessage,
                 org.apache.struts.util.ResponseUtils,
                 javax.mail.Flags,
                 edu.ufl.osg.webmail.Constants,
                 java.util.ArrayList,
                 java.util.List,
                 java.util.Arrays,
                 javax.mail.BodyPart,
                 javax.mail.Part"%>
<%@page contentType="text/html"%>
<html>
<head>
 <title>Message Structure</title>
 <style type="text/css">
    ul {
        border : solid;
        padding-right : 1em;
    }
 </style>
</head>
<body>

<%
    IMAPFolder folder = (IMAPFolder)Util.getFolder(session, request.getParameter("folder"));
    long uid = Long.parseLong(request.getParameter("uid"));
    Message message = folder.getMessageByUID(uid);

    Flags f = new Flags();
    f.add("HasAttachment");
    f.add("NoAttachment");
    message.setFlags(f, false);

    flagMessageWithAttachments(message);

    try {
        show(message, out);
    } catch (Throwable t) {
        out.println(t.toString());
    } finally {
        Util.releaseFolder(folder);
    }
%>

</body>
</html>
<%!
    private static void flagMessageWithAttachments(Message message) throws MessagingException {
        boolean hasAttachment = false;
        boolean seen = message.isSet(Flags.Flag.SEEN);
        Flags flags = message.getFlags();

        // No pre-existing flags

        if (message.isMimeType("text/*")) {
            System.err.println("message.isMimeType('text/*'))");
            hasAttachment = false;
        } else if (message.isMimeType("multipart/*")) {
            System.err.println("message.isMimeType('multipart/*')");
            try {
                MimeMultipart mmp = (MimeMultipart)message.getContent();
                hasAttachment = hasAttachment(mmp);
            } catch (Exception e) {
                // Do nothing
            }
        }

        System.err.println("hasAttachment: " + hasAttachment);
        if (hasAttachment) {
            flags = new Flags(Constants.MESSAGE_FLAG_HAS_ATTACHMENT);
            message.setFlags(flags, true);
        } else {
            flags = new Flags(Constants.MESSAGE_FLAG_NO_ATTACHMENT);
            message.setFlags(flags, true);
        }

        // Reset the seen state if we need to.
        if (!seen) {
            message.setFlag(Flags.Flag.SEEN, false);
        }

    }

    public static boolean hasAttachment(MimeMultipart mmp) throws MessagingException, IOException {
        for (int i = 0; i < mmp.getCount(); i++) {
            BodyPart part = mmp.getBodyPart(i);
            //if (part.getDisposition() == null || part.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)) {
            //    System.err.println("part.getDisposition() attachment: " + true);
            //    return true;
            //}
            if (!part.isMimeType("text/*") && !part.isMimeType("multipart/*")) {
                System.err.println("part.isMimeType() attachment: " + true);
                return true;
            }

            boolean ha = false;
            if (part.isMimeType("multipart/*")) {
                ha = hasAttachment((MimeMultipart)part.getContent());
            }
            if (ha) {
                return ha;
            }
        }
        return false;
    }


    void show(Object o, JspWriter out) throws IOException, MessagingException {
        if (o instanceof String) {
            showString((String)o, out);

        } else if (o instanceof MimeMessage) {
            showMimeMessage((MimeMessage)o, out);

        } else if (o instanceof MimeMultipart) {
            showMimeMultipart((MimeMultipart)o, out);

        } else if (o instanceof MimeBodyPart) {
            showMimeBodyPart((MimeBodyPart)o, out);

        } else {
            out.println("<b>I cannot currently handle: " + o.getClass().getName() + "</b>");
        }
    }

    void showString(String o, JspWriter out) throws IOException {
        out.println("<ul style='border-color : black'>" + o.getClass().getName());
        out.println("<li>length(): " + o.length() + "</li>");
        out.println("</ul>");
    }

    void showMimeMessage(MimeMessage o, JspWriter out) throws IOException, MessagingException {
        out.println("<ul style='border-color : blue'>" + o.getClass().getName());
        out.println("<li>getContentType(): " + o.getContentType() + "</li>");
        out.println("<li>getSubject(): " + ResponseUtils.filter(o.getSubject()) + "</li>");

        List l = new ArrayList();
        Flags f = o.getFlags();
        l.addAll(Arrays.asList(f.getUserFlags()));

        out.println("<li>getFlags(): " + l.toString() + "</li>");

        out.println("<li>getContent():");
        show(o.getContent(), out);
        out.println("</li>");
        out.println("</ul>");
    }

    void showMimeMultipart(MimeMultipart o, JspWriter out) throws IOException, MessagingException {
        out.println("<ul style='border-color : red'>" + o.getClass().getName());
        out.println("<li>getContentType(): " + o.getContentType() + "</li>");
        out.println("<li>getCount(): " + o.getCount() + "</li>");

        for (int i=0; i < o.getCount(); i++) {
            out.println("<li>Part " + i);
            show(o.getBodyPart(i), out);
            out.println("</li>");
        }

        out.println("</ul>");
    }

    void showMimeBodyPart(MimeBodyPart o, JspWriter out) throws IOException, MessagingException {
        out.println("<ul style='border-color : green'>" + o.getClass().getName());
        out.println("<li>getContentType(): " + o.getContentType() + "</li>");
        out.println("<li>getContentID(): " + ResponseUtils.filter(o.getContentID()) + "</li>");

        Enumeration hEnum = o.getAllHeaderLines();
        out.println("<li>getAllHeaderLines(): <ul>");
        while (hEnum.hasMoreElements()) {
            out.println("<li>" + ResponseUtils.filter(hEnum.nextElement().toString()) + "</li>");
        }
        out.println("</ul></li>");

        out.println("<li>getContent():");
        show(o.getContent(), out);
        out.println("</li>");

        out.println("</ul>");
    }


%>