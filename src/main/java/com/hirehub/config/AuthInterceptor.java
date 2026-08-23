package com.hirehub.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // Allow static resources and public endpoints
        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/") ||
            uri.startsWith("/webjars/") || uri.equals("/login") || uri.equals("/register") ||
            uri.equals("/") || uri.equals("/error")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        String userEmail = (session != null) ? (String) session.getAttribute("userEmail") : null;
        String userRole = (session != null) ? (String) session.getAttribute("userRole") : null;

        if (userEmail == null) {
            response.sendRedirect("/login");
            return false;
        }

        // Admin-only pages
        if (uri.startsWith("/admin")) {
            if (!"Admin".equalsIgnoreCase(userRole)) {
                response.sendRedirect("Recruiter".equalsIgnoreCase(userRole) ? "/recruiter/dashboard" : "/dashboard");
                return false;
            }
        }

        // Recruiter-only pages
        if (uri.startsWith("/recruiter") || uri.equals("/post-job") || uri.equals("/jobs/post") || uri.startsWith("/jobs/post") || uri.equals("/manage-jobs") || uri.equals("/applicants")) {
            if (!"Recruiter".equalsIgnoreCase(userRole) && !"Admin".equalsIgnoreCase(userRole)) {
                response.sendRedirect("/dashboard");
                return false;
            }
        }

        return true;
    }
}
