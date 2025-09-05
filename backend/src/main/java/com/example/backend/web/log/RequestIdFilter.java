package com.example.backend.web.log;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class RequestIdFilter implements Filter {
    private static final String HDR = "X-Request-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String rid = req.getHeader(HDR);
        if (rid == null || rid.isBlank()) rid = UUID.randomUUID().toString();
        MDC.put("requestId", rid);  // ログパターンに %X{requestId} を入れる

        try {
            res.setHeader(HDR, rid);
            chain.doFilter(request, response);
        } finally {
            MDC.remove("requestId");
        }
    }
}