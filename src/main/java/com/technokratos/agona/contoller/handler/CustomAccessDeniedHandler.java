package com.technokratos.agona.contoller.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(TemplateEngine templateEngine, ObjectMapper objectMapper) {
        this.templateEngine = templateEngine;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        boolean isAjax = "XMLHttpRequest".equals(requestedWith)
                || (accept != null && accept.contains("application/json") && !accept.contains("text/html"));

        if (isAjax) {
            response.setContentType("application/json;charset=UTF-8");
            ExceptionMessage body = ExceptionMessage.builder()
                    .exceptionName("AccessDeniedException")
                    .message("Доступ запрещён")
                    .build();
            objectMapper.writeValue(response.getWriter(), body);
        } else {
            response.setContentType("text/html;charset=UTF-8");
            var app = JakartaServletWebApplication.buildApplication(request.getServletContext());
            var exchange = app.buildExchange(request, response);
            WebContext ctx = new WebContext(exchange, request.getLocale());
            ctx.setVariable("status", 403);
            ctx.setVariable("title", "Доступ запрещён");
            ctx.setVariable("message", "У вас нет прав для просмотра этой страницы.");
            templateEngine.process("error/error", ctx, response.getWriter());
        }
    }
}
