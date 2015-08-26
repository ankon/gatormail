<%@ page import="edu.ufl.osg.webmail.util.Util,
                 java.util.Map,
                 java.util.Iterator,
                 edu.ufl.osg.webmail.User,
                 edu.ufl.osg.webmail.util.RequestTimerFilter,
                 java.util.Date,
                 java.util.Timer,
                 java.util.TimerTask,
                 java.util.List,
                 java.util.HashMap,
                 java.util.Collection,
                 java.util.Arrays"%>
<%@page contentType="text/html; charset=utf-8"%>
<%!

    private static final Date statsStart = new Date();

    static long minFreeMemory = Runtime.getRuntime().freeMemory();
    static long maxFreeMemory = Runtime.getRuntime().freeMemory();

    static long minTotalMemory = Runtime.getRuntime().totalMemory();
    static long maxTotalMemory = Runtime.getRuntime().totalMemory();

    static long minUsedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    static long maxUsedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

    static int minSessions = Util.getActiveSessions().size();
    static int maxSessions = Util.getActiveSessions().size();

    static final Timer timer = new Timer(true);
    static {
        timer.schedule(new TimerTask() {
            public void run() {
                updateStats();
            }
        }, 0, 60 * 1000);
    }

    static void updateStats() {
        int sessions = Util.getActiveSessions().size();
        minSessions = Math.min(minSessions, sessions);
        maxSessions = Math.max(maxSessions, sessions);

        // It's desireable to gc() when there are no users to find out what the min used mem is
        if (sessions == 0) {
            System.runFinalization();
            System.gc();
            minUsedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        }

        long freeMem = Runtime.getRuntime().freeMemory();
        minFreeMemory = Math.min(minFreeMemory, freeMem);
        maxFreeMemory = Math.max(maxFreeMemory, freeMem);

        long totalMem = Runtime.getRuntime().totalMemory();
        minTotalMemory = Math.min(minTotalMemory, totalMem);
        maxTotalMemory = Math.max(maxTotalMemory, totalMem);

        long usedMem = totalMem - freeMem;
        minUsedMemory = Math.min(minUsedMemory, usedMem);
        maxUsedMemory = Math.max(maxUsedMemory, usedMem);
    }
%>
<html>
<head>
 <title>GatorMail Stats</title>
 <link rel="start" href="stats.jsp?gc=true" title="Run GC"/>
 <link rel="up" href="stats.jsp" title="Stats"/>
 <link rel="alternate" href="stats.jsp?activity=true" title="Show Activity"/>
 <meta http-equiv="refresh" content="60;URL=stats.jsp"/>
 <style type="text/css">
    td {
        text-align : center;
    }
    .timeings tbody:hover {
        color : blue;
    }
 </style>
</head>
<body>

<%   // if stats page is being used for quick stats query
     if (request.getParameter("users") != null) {
%>
<%= Util.getActiveSessions().size() %></body></html>
<%
        return;
     }

    if (request.getParameter("setMaxAvg") != null) {
        final double maxAvg = Double.parseDouble(request.getParameter("setMaxAvg"));
        RequestTimerFilter.setMaxAverage(maxAvg);
    }
%>

<%
    boolean showActivity = false;
    final Map activityCounts = new HashMap();
    if (request.getParameter("activity") != null) {
        try {
            showActivity = Boolean.valueOf(request.getParameter("activity")).booleanValue();
        } catch (Exception e) {
            // Nothing
        }
    }
//     if (request.getParameter("delay") != null) {
//          try {
//             int delay = new Integer(request.getParameter("delay")).intValue();
//             if (delay >= 0 && delay <= 10) {
//                Util.delay = delay;
//             }
//          } catch (Exception e) {}
//     }
    if (request.getParameter("gc") != null) {
        System.runFinalization();
        System.gc(); // This artifically compacts memory, which may or may not be desired during testing.
    }
    updateStats();
    List sessions = Util.getActiveSessions();
    int users = 0;
    long totalTime = 0;
    long maxLoginTime = 0;
%>
<table border="1">
 <tr>
  <td><a href="stats.jsp?gc=true" title="Run the finalizer and garbage collecter so we can get an idea about minimum memory usage.">System.gc()</a></td>
  <th>Min</th>
  <th>Current</th>
  <th>Max</th>
<%
    if (sessions.size() != 0) {
%>
  <td rowspan="99" width="250">
      <p style="font-weight:bolder;"><%= System.getProperty("jasclone", "") %></p>
      <p>
   Assuming the "Min Used Mem" is the baseline the container needs then each
   user is using an average of
   <%= (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - minUsedMemory) / sessions.size() / 1024 %>K
   of memory.
      </p>
  </td>
<%
    }
%>
  <td rowspan="99">
   This Webapp started at: <br/>
   <%= Util.getWebappinit() %> <br/>
   <br/>
   Stat collection started at:<br/>
   <%= statsStart %><br/>
   <br/>
   Last updated on:<br/>
   <%= new Date() %>
  </td>
  <td rowspan="99">
      <p>
          Request Time Averages:
      </p>
      <p>
          Current:
          <%= RequestTimerFilter.getCurrentResponseTime() %> sec
      </p>
      <p>
          Five Min:
          <%= RequestTimerFilter.getFiveMinuteResponseTime() %> sec
      </p>
      <p>
          Fifteen Min:
          <%= RequestTimerFilter.getFifteenMinuteResponseTime() %> sec
      </p>
      <p>
          <%= RequestTimerFilter.isOverLimit() ? "<span style=\"color:red\">" : "" %>
          Max Avg:
          <%= RequestTimerFilter.getMaxAvg() %>
          <%= RequestTimerFilter.isOverLimit() ? "</span>" : "" %>
      </p>
  </td>
 </tr>
 <tr>
  <th>Users</th>
  <td><%= minSessions %></td>
  <td><%= sessions.size() %></td>
  <td><%= maxSessions %></td>
 </tr>
 <tr>
  <th>Free Mem</th>
  <td><%= minFreeMemory / 1024 / 1024 %>M</td>
  <td><%= Runtime.getRuntime().freeMemory() / 1024 / 1024 %>M</td>
  <td><%= maxFreeMemory / 1024 / 1024 %>M</td>
 </tr>
 <tr>
  <th>Used Mem</th>
  <td><%= minUsedMemory / 1024 / 1024 %>M</td>
  <td><%= (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 %>M</td>
  <td><%= maxUsedMemory / 1024 / 1024 %>M</td>
 </tr>
 <tr>
  <th>Total Mem</th>
  <td><%= minTotalMemory / 1024 / 1024 %>M</td>
  <td><%= Runtime.getRuntime().totalMemory() / 1024 / 1024 %>M</td>
  <td><%= maxTotalMemory / 1024 / 1024 %>M</td>
 </tr>
</table>

<hr/>

<%
    final double[] averages = RequestTimerFilter.getAllAverages();
    final long[] cumulativeTime = RequestTimerFilter.getCumulativeTime();
    final long[] hits = RequestTimerFilter.getHits();
%>
<table border="1" class="timeings">
    <tbody>
        <tr>
            <th>Minute</th>
<%
    for (int i=0; i < 15; i++) {
%>
            <td><%= i < 10 ? "0" + i : String.valueOf(i) %></td>
<%
    }
%>
        </tr>
        <tr>
            <th>Average</th>
<%
    for (int i=0; i < 15; i++) {
        String avg = String.valueOf(averages[i]);
        if (avg.length() > 6) avg = avg.substring(0, 6);
%>
            <td title="<%= cumulativeTime[i] + " / " + hits[i] %>"><%= avg %></td>
<%
    }
%>
        </tr>
    </tbody>
    <tbody>
        <tr>
            <th>Minute</th>
<%
    for (int i=15; i < 30; i++) {
%>
            <td><%= i %></td>
<%
    }
%>
        </tr>
        <tr>
            <th>Average</th>
<%
    for (int i=15; i < 30; i++) {
        String avg = String.valueOf(averages[i]);
        if (avg.length() > 6) avg = avg.substring(0, 6);
%>
            <td title="<%= cumulativeTime[i] + " / " + hits[i] %>"><%= avg %></td>
<%
    }
%>
        </tr>
    </tbody>
    <tbody>
        <tr>
            <th>Minute</th>
<%
    for (int i=30; i < 45; i++) {
%>
            <td><%= i %></td>
<%
    }
%>
        </tr>
        <tr>
            <th>Average</th>
<%
    for (int i=30; i < 45; i++) {
        String avg = String.valueOf(averages[i]);
        if (avg.length() > 6) avg = avg.substring(0, 6);
%>
            <td title="<%= cumulativeTime[i] + " / " + hits[i] %>"><%= avg %></td>
<%
    }
%>
        </tr>
    </tbody>
    <tbody>
        <tr>
            <th>Minute</th>
<%
    for (int i=45; i < 60; i++) {
%>
            <td><%= i %></td>
<%
    }
%>
        </tr>
        <tr>
            <th>Average</th>
<%
    for (int i=45; i < 60; i++) {
        String avg = String.valueOf(averages[i]);
        if (avg.length() > 6) avg = avg.substring(0, 6);
%>
            <td title="<%= cumulativeTime[i] + " / " + hits[i] %>"><%= avg %></td>
<%
    }
%>
        </tr>
    </tbody>
</table>

<hr/>

<a href="stats.jsp?activity=true">Show Recent User Activity</a>
(<a href="stats.jsp?activity=true#urlCounts">and scroll to url counts</a>)

<ul>
<%
    Iterator iter = sessions.iterator();
    while (iter.hasNext()) {
        User user = (User)iter.next();
        if (user == null) {
            continue;
        }

        users++;
        totalTime += System.currentTimeMillis() - user.getBoundTime();

        out.print("<li>");
        out.print(user.getUsername());
        out.print(" has been logged in for ");

        long loginTime = Math.round((double)(System.currentTimeMillis() - user.getBoundTime()) / 1000d / 60d);
        maxLoginTime = Math.max(maxLoginTime, loginTime);

        out.print(loginTime);
        out.println(" minutes.");

        if (showActivity) {
            List recentActivity = user.getRecentAcitivity();
            Iterator activityIterator = recentActivity.iterator();
            out.println("<ul>");
            while (activityIterator.hasNext()) {
                User.Activity activity = (User.Activity)activityIterator.next();
                out.print("<li>");
                long activiltyTime =  Math.round((double)(System.currentTimeMillis() - activity.getTime()) / 1000d / 60d);
                out.print(activiltyTime + "m ");
                String requestUrl = activity.getRequest();
                out.print(requestUrl);
                updateActivity(activityCounts, requestUrl);

                out.println("</li>");
            }
            out.println("</ul>");
        }
        out.println("</li>");
    }
%>
</ul>

<hr/>

<%
    if (users != 0) {
%>
<p>
 Each user is logged in for an average of
 <%= Math.round(((double)totalTime / users) / 1000d / 60d) %> minutes
 with longest logged in time being
 <%= maxLoginTime %> minutes.
</p>
<%
    }
%>
<hr/>

<%
    if (showActivity) {
        out.println("<a name=\"urlCounts\">Instant url count:</a>");
        Collection values = activityCounts.values();
        ActStat[] stats = (ActStat[])values.toArray(new ActStat[0]);
        Arrays.sort(stats);
        List statsList = Arrays.asList(stats);
        Iterator statsIterator = statsList.iterator();

        out.println("<ol>");
        while (statsIterator.hasNext()) {
            ActStat stat = (ActStat)statsIterator.next();
            out.print("<li>");
            out.print(stat.count);
            out.print(" ");
            out.print(stat.url);
            out.println("</li>");
        }
        out.println("</ol>");
    }
%>

</body>
</html>
<%!
    private void updateActivity(Map map, String url) {
        ActStat stat = (ActStat)map.get(url);
        if (stat == null) {
            stat = new ActStat(url);
            map.put(url, stat);
        }
        stat.count++;
    }

    private static class ActStat implements Comparable {
        final String url;
        int count = 0;
        ActStat(String url) {
            this.url = url;
        }

        public int compareTo(Object o) {
            ActStat stat = (ActStat)o;
            return stat.count - count;
        }
    }
%>
