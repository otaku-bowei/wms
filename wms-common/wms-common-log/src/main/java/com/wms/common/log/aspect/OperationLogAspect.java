package com.wms.common.log.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.common.log.annotation.OperationLog;
import com.wms.common.log.event.OperationLogEvent;
import com.wms.common.security.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;

/**
 * 操作日志切面
 *
 * <p>采集操作信息并发布事件；敏感字段（密码类）统一脱敏为 ******。
 *
 * @author WMS
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    /** 需要脱敏的参数名 */
    private static final List<String> SENSITIVE_KEYS = List.of("password", "oldPassword", "newPassword", "confirmPassword");

    private static final String MASK = "******";

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object record(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        boolean success = true;
        String errorMsg = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            publishEvent(joinPoint, operationLog, success, errorMsg, System.currentTimeMillis() - start);
        }
    }

    private void publishEvent(ProceedingJoinPoint joinPoint, OperationLog annotation,
                              boolean success, String errorMsg, long costMs) {
        try {
            HttpServletRequest request = currentRequest();
            OperationLogEvent event = OperationLogEvent.builder()
                    .userId(UserContext.getUserId())
                    .username(UserContext.getUsername())
                    .module(annotation.module())
                    .operationType(annotation.type().getCode())
                    .description(annotation.description())
                    .requestParams(serializeArgs(joinPoint.getArgs()))
                    .requestUri(request == null ? null : request.getRequestURI())
                    .ipAddress(request == null ? null : request.getRemoteAddr())
                    .success(success)
                    .errorMsg(errorMsg)
                    .costMs(costMs)
                    .build();
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.warn("操作日志采集失败：{}", e.getMessage());
        }
    }

    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(args);
            return maskSensitive(json);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 简单脱敏：将 password 类字段的值替换为掩码
     */
    private String maskSensitive(String json) {
        String result = json;
        for (String key : SENSITIVE_KEYS) {
            result = result.replaceAll("\"" + key + "\"\\s*:\\s*\"[^\"]*\"", "\"" + key + "\":\"" + MASK + "\"");
        }
        return result;
    }

    private HttpServletRequest currentRequest() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            return servletAttributes.getRequest();
        }
        return null;
    }

    /**
     * 保留 Map 结构序列化能力（供扩展使用）
     */
    @SuppressWarnings("unused")
    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return null;
        }
    }
}
