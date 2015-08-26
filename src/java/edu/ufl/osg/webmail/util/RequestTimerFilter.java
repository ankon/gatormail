/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2006 The Open Systems Group / University of Florida
 *
 * GatorMail is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * GatorMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GatorMail; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ufl.osg.webmail.util;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Filter that times how long requests are taking.
 *
 * @author sandymac
 * @since Jan 10, 2006
 * @version $Revision: 1.3 $
 */
public class RequestTimerFilter implements Filter {

    private static final Object LOCK = new Object();

    /**
     * Called by the web container to indicate to a filter that it is being placed into
     * service. The servlet container calls the init method exactly once after instantiating the
     * filter. The init method must complete successfully before the filter is asked to do any
     * filtering work. <br><br>

     * The web container cannot place the filter into service if the init method either<br>
     * 1.Throws a ServletException <br>
     * 2.Does not return within a time period defined by the web container
     */
    public void init(final FilterConfig filterConfig) throws ServletException {
        final String maxAvg = filterConfig.getServletContext().getInitParameter("login.request.max.avg");
        if (maxAvg != null) {
            RequestTimerFilter.maxAvg = Double.parseDouble(maxAvg);
        }
    }

    /**
     * The <code>doFilter</code> method of the Filter is called by the container
     * each time a request/response pair is passed through the chain due
     * to a client request for a resource at the end of the chain. The FilterChain passed in to this
     * method allows the Filter to pass on the request and response to the next entity in the
     * chain.<p>
     * A typical implementation of this method would follow the following pattern:- <br>
     * 1. Examine the request<br>
     * 2. Optionally wrap the request object with a custom implementation to
     * filter content or headers for input filtering <br>
     * 3. Optionally wrap the response object with a custom implementation to
     * filter content or headers for output filtering <br>
     * 4. a) <strong>Either</strong> invoke the next entity in the chain using the FilterChain object (<code>chain.doFilter()</code>), <br>
     ** 4. b) <strong>or</strong> not pass on the request/response pair to the next entity in the filter chain to block the request processing<br>
     ** 5. Directly set headers on the response after invocation of the next entity in the filter chain.
     **/
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final long startTime = System.currentTimeMillis();
        final long endTime;
        try {
            chain.doFilter(request, response);
        } finally {
            endTime = System.currentTimeMillis();
            updateBuckets(startTime, endTime);
        }
    }

    /**
     * Called by the web container to indicate to a filter that it is being taken out of service. This
     * method is only called once all threads within the filter's doFilter method have exited or after
     * a timeout period has passed. After the web container calls this method, it will not call the
     * doFilter method again on this instance of the filter. <br><br>
     *
     * This method gives the filter an opportunity to clean up any resources that are being held (for
     * example, memory, file handles, threads) and make sure that any persistent state is synchronized
     * with the filter's current state in memory.
     */
    public void destroy() {
    }

    private static long[] cumulativeTime = new long[60];
    private static long[] hits = new long[60];
    private static volatile int lastBucket = 0;
    private static volatile double maxAvg = 0;

    public static boolean isOverLimit() {
        return maxAvg > 0 && calcRangeResponseTime(3) > maxAvg;
    }

    private static int getCurrentBucket() {
        return (int)(System.currentTimeMillis() / (60 * 1000) % 60);
    }

    private static double getResponseTime(final int bucket) {
        final long cumulativeTime;
        final long hits;
        synchronized (LOCK) {
            cumulativeTime = RequestTimerFilter.cumulativeTime[bucket];
            hits = RequestTimerFilter.hits[bucket];
        }
        return calcAvgSeconds(hits, cumulativeTime);
    }

    private static double calcAvgSeconds(final long hits, final long cumulativeTime) {
        return hits > 0 ? (double)cumulativeTime / (double)hits / (double)1000 : 0;
    }

    private static void updateBuckets(final long startTime, final long endTime) {
        final int bucket = (int)(endTime / (60 * 1000) % 60);
        final long difference = endTime - startTime;
        synchronized (LOCK) {
            while (lastBucket != bucket) {
                lastBucket = (lastBucket + 1) % 60;
                cumulativeTime[lastBucket] = 0;
                hits[lastBucket] = 0;
            }
            cumulativeTime[bucket] += difference;
            hits[bucket] += 1;
        }
    }

    public static double getCurrentResponseTime() {
        final int bucket = getCurrentBucket();
        return getResponseTime(bucket);
    }

    public static double getPreviousResponseTime() {
        int bucket = getCurrentBucket() - 1;
        if (bucket < 0) bucket += 60;
        return getResponseTime(bucket);
    }

    public static double getFiveMinuteResponseTime() {
        return calcRangeResponseTime(5);
    }

    public static double getFifteenMinuteResponseTime() {
        return calcRangeResponseTime(15);
    }

    public static double calcRangeResponseTime(final int minutes) {
        final int bucket = getCurrentBucket();
        long cumulativeTime = 0;
        long hits = 0;
        synchronized (LOCK) {
            for (int i = 0; i < minutes; i++) {
                int b = bucket - i;
                if (b < 0) {
                    b += 60;
                }
                cumulativeTime += RequestTimerFilter.cumulativeTime[b];
                hits += RequestTimerFilter.hits[b];
            }
        }
        return calcAvgSeconds(hits, cumulativeTime);
    }

    public static double[] getAllAverages() {
        final double[] averages = new double[60];
        synchronized (LOCK) {
            for (int i = 0; i < cumulativeTime.length; i++) {
                averages[i] = calcAvgSeconds(hits[i], cumulativeTime[i]);
            }
        }
        return averages;
    }

    public static long[] getCumulativeTime() {
        return (long[])cumulativeTime.clone();
    }

    public static long[] getHits() {
        return (long[])hits.clone();
    }

    public static void setMaxAverage(final double maxAvg) {
        RequestTimerFilter.maxAvg = maxAvg;
    }

    public static double getMaxAvg() {
        return maxAvg;
    }
}