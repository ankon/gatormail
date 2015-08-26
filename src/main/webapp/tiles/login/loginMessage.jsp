<%@page contentType="text/html" import="org.apache.struts.action.ActionErrors"%>
<%@taglib uri="/tags/struts-html" prefix="html"%>
<%@taglib uri="/tags/struts-bean" prefix="bean"%>
<h1>Welcome to the new GatorLink Webmail</h1>

<p>
 <html:link forward="inbox">Continue to your INBOX</html:link>
</p>

<p>
 This is a brand new webmail written by
 <html:link href="http://open-systems.ufl.edu/">The Open Systems Group</html:link>.
 This page will be removed soon and you will go directly to your
 <html:link forward="inbox">INBOX</html:link> after login.

</p>

<h2>Message Filtering</h2>

<p>
 There are quite a few changes in this version but the most interesting one is
 the ability to filter the list of messages in a folder. I hope this feature is
 self explanatory but just in case below is a quick rundown.
</p>

<p>
 You should notice a new select box and text input box in the light blue bar at
 the top of the list of messages in a folder. In this drop down list you should
 see some choices on how you can filter the list of messages. The first choice
 doesn't do anything, it's just an informative message.
</p>

<p>
 Three of the choices end in a colon '<b>:</b>' and these choices require you to
 input something in the text input box. For example the "Subject and Sender:"
 choice will do a case insensitive search on the subjects and the from fields of
 each message in the current folder.
</p>

<p>
 The remaining choices don't require any text input. Actually they ignore the
 text input box completely. Except the last two each should be obvious about
 what it does. If it doesn't make sense just try it out, trust me, they don't
 hurt. ;-)
</p>

<p>
 "Senders in Address Book" and "Senders not in Address Book" compare the address
 an email is from with all the entries in your address book. If one matches it
 either lists or doesn't list that message. What may be a little confusing if
 you don't have anything in your address book. In that case these choices don't
 do anything.
</p>

<%--
<p>
 The address book based filters should help you manage spam. We have another
 project in the works that will give you more power in the fight against spam
 but it's gonna be at least a few months before we'll have that ready for
 production. If you have a good idea for a filter be sure to send a feature
 request from the feedback link and if we think other people will like it we'll
 add it to a future version of GatorMail.
</p>
--%>

<p>
 Feedback, both positive and negative but preferably constructive, is welcomed.
 There is a link at the top of most pages to aid you in that process.
</p>

<%--

<hr width="50%"/>

<p>
  <i>Programmers and developers please read:</i>
</p>

<p>
  As of April 18th GatorMail will no longer be under active development, the
  project will be in maintenance mode while I work on another frequently requested
  service.
</p>

<p>
  On the 18th the source code to GatorMail will be made available under the GPL
  license. There are many improvements and enhancements that can be made and I
  encourage and welcome patches.
</p>

<p>
  If you think having you name listed on the about page is cool or maybe you
  just want to help make GatorMail better for your own personal use then here is
  your chance.
</p>

--%>

<p>
 <html:link forward="inbox">Continue to your INBOX</html:link>
</p>