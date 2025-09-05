package com.example.backend.web.log;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AccessLogFilter implements Filter{
    private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            HttpServletResponse res = (HttpServletResponse) response;
            long ms = System.currentTimeMillis() - start;
            // Authorization等の機密ヘッダは出さない
            log.info("{} {} {} {}ms", req.getMethod(), req.getRequestURI(), res.getStatus(), ms);
        }
    }
}
