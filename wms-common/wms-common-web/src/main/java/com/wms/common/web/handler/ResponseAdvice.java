package com.wms.common.web.handler;

import com.wms.common.core.result.R;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一响应增强
 *
 * <p>为所有 {@link R} 响应自动填充 timestamp 与 traceId。
 *
 * @author WMS
 */
@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                 Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                 ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof R<?> r) {
            if (r.getTimestamp() == 0) {
                r.setTimestamp(System.currentTimeMillis());
            }
            if (r.getTraceId() == null) {
                r.setTraceId(MDC.get(TraceIdFilter.TRACE_ID));
            }
        }
        return body;
    }
}
