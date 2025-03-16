package common;

import io.jsonwebtoken.Claims;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        if (uri.contains("/_dashboard")) {
            // skip this filter if employee login
            chain.doFilter(request, response);
            return;
        }

        System.out.println("LoginFilter: " + uri);

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(uri)) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        String token = JwtUtil.getCookieValue(httpRequest, "jwtToken");
        Claims claims = JwtUtil.validateToken(token);

        if (claims != null){
            httpRequest.setAttribute("claims", claims);

            chain.doFilter(request,response);
        } else {
            httpResponse.sendRedirect(httpRequest.getContextPath()+"/login.html");
        }
        // Redirect to login page if the "user" attribute doesn't exist in session
//        if (httpRequest.getSession().getAttribute("user") == null) {
//            httpResponse.sendRedirect(httpRequest.getContextPath()+"/login.html");
//        } else {
//            chain.doFilter(request, response);
//        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("api/login");
        allowedURIs.add("api/dashboardlogin");
        allowedURIs.add("styles.css");
    }

    public void destroy() {
        // ignored.
    }

}
