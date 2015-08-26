package edu.ufl.osg.webmail.util;

import javax.servlet.*;
import javax.mail.Folder;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Checks that any open folders were closed at the end of the request.
 */
public class FolderCloserFilter implements Filter {
    private static ThreadLocal tl = new ThreadLocal() {
        protected Object initialValue() {
            return new ArrayList();
        }
    };

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final List l = (List)tl.get();

        try {
            chain.doFilter(request, response);

        } finally {

            final Iterator iter = l.iterator();
            while (iter.hasNext()) {
                final Folder f = (Folder)iter.next();
                if (f.isOpen()) {
                    if (Util.traceProtocol) {
                        Util.addProtocolMarkers(f, (new Exception()).getStackTrace());
                        System.err.println(f + " (" + System.identityHashCode(f)  + ") was left open!");
                    }
                    try {
                        f.close(false);
                    } catch (MessagingException e) {
                        // swallowed
                    }
                }
                iter.remove();

            }
        }

    }

    public static void closeFolder(Folder f) {
        List l = (List)tl.get();
        l.add(f);
    }

    public void destroy() {
    }
}
