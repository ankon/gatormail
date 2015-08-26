package edu.ufl.osg.webmail.util;

import org.apache.log4j.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import edu.ufl.osg.webmail.Constants;
import edu.ufl.osg.webmail.User;

/**
 * Logs the IP address used by remote users.
 *
 * @author Sandy McArthur
 */
public class LogRemoteIpAddressFilter implements Filter {
    private static final Logger logger = Logger.getLogger(LogRemoteIpAddressFilter.class.getName());

    public void init(final FilterConfig config) throws ServletException {
    }

    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } finally {
            // Log after the main request has happened so we can catch the user name during login
            if (request instanceof HttpServletRequest) {
                final HttpServletRequest httpRequest = (HttpServletRequest)request;
                final HttpSession session = httpRequest.getSession(false);

                // If there is no session, we don't know who this is.
                if (session != null) {
                    final User user = (User)session.getAttribute(Constants.USER_KEY);

                    // If we don't know who is logged in, don't bother logging anything
                    if (user != null) {

                        final String ip = httpRequest.getRemoteAddr();
                        // try to pick up if the user is using a proxy too
                        final String via = httpRequest.getHeader("Via");
                        final String xForwardedFor = httpRequest.getHeader("X-Forwarded-For");

                        // create a stable, unique key for IP + proxy combinations
                        final StringBuffer keyBuffer = new StringBuffer("observed-");
                        keyBuffer.append(ip);
                        if (via != null && via.length() > 0) {
                            keyBuffer.append("-").append(via);
                        }
                        if (xForwardedFor != null && xForwardedFor.length() > 0) {
                            keyBuffer.append("-").append(xForwardedFor);
                        }

                        // if we haven't logged the IP already
                        if (session.getAttribute(keyBuffer.toString()) == null) {

                            final StringBuffer messageBuffer = new StringBuffer(128);
                            messageBuffer.append("user: ").append(user.getEmail());
                            messageBuffer.append(" observed using: ").append(ip);
                            if (via != null && via.length() > 0) {
                                messageBuffer.append("  Via: ").append(via);
                            }
                            if (xForwardedFor != null && xForwardedFor.length() > 0) {
                                messageBuffer.append("  X-Forwarded-For: ").append(xForwardedFor);
                            }

                            // Collapse whitespace and newlines into 1 space
                            final String message = messageBuffer.toString().replaceAll("\\s+", " ");

                            session.setAttribute(keyBuffer.toString(), message);
                            logger.info(message);
                        }
                    }
                }
            }
        }
    }

    public void destroy() {
    }
}
