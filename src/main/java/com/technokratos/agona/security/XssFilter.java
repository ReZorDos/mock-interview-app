package com.technokratos.agona.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Integer.MIN_VALUE + 1)
public class XssFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        if (isMultipartRequest(request)) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(new XssRequestWrapper(request), response);
        }
    }

    private boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null
                && contentType.toLowerCase().startsWith("multipart/");
    }

}
