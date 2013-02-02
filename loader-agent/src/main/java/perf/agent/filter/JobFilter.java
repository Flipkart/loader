package perf.agent.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 2/1/13
 * Time: 3:32 PM
 * Forward All Job Specifc Operations to Job/Loader Process
 */
public class JobFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //Nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            String redirectUri = redirectURI(servletRequest);
            if (redirectUri != null) {
                HttpServletResponse response = (HttpServletResponse) servletResponse;
                response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                response.setHeader("Location", redirectUri);
                response.setHeader("Connection", "close");
                return;
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private String redirectURI(ServletRequest request) {

        return "";
    }

    @Override
    public void destroy() {
        //Nothing
    }
}
