<%@page contentType="text/html" import="edu.ufl.osg.webmail.*,javax.mail.*,java.util.*,javax.mail.internet.*"%>
<%@taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%--
This document defines a "parallel" subtype of the multipart Content- Type. This
type is syntactically identical to multipart/mixed, but the semantics are
different. In particular, in a parallel entity, the order of body parts is not
significant.

A common presentation of this type is to display all of the parts simultaneously
on hardware and software that are capable of doing so. However, composing agents
should be aware that many mail readers will lack this capability and will show
the parts serially in any event. 
--%>