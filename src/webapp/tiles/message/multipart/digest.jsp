<%@page contentType="text/html"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%--
This document defines a "digest" subtype of the multipart Content- Type. This
type is syntactically identical to multipart/mixed, but the semantics are
different. In particular, in a digest, the default Content-Type value for a body
part is changed from "text/plain" to "message/rfc822". This is done to allow a
more readable digest format that is largely compatible (except for the quoting
convention) with RFC 934.

// TODO: This seems to work but I'm not sure if completely correct.
--%>
<!-- <div class="messageMultipartDigest"> -->
<tiles:insert name="message.multipart/mixed"/>
<!-- </div> -->