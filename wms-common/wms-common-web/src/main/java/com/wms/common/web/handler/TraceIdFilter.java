package com.wms.common.web.handler;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * 链路追踪 ID 过滤器
 *
 * <p>优先使用上游透传的 X-Trace-Id，否则生成新的 traceId，写入 MDC 供日志与响应体使用。
 *
 * @author WMS
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter implements Filter {

    public static final String TRACE_ID = "traceId";
    public static final String TRACE_HEADER = "X-Trace-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            String traceId = resolveTraceId(request);
            MDC.put(TRACE_ID, traceId);
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
        }
    }

    private String resolveTraceId(ServletRequest request) {
        if (request instanceof HttpServletRequest httpRequest) {
            String header = httpRequest.getHeader(TRACE_HEADER);
            if (header != null && !header.isBlank()) {
                return header;
            }
        }
        return Optional.ofNullable(MDC.get(TRACE_ID)).orElseGet(() -> UUID.randomUUID().toString().replace("-", ""));
    }
}
