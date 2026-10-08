package com.wms.common.log.event;

import lombok.Builder;
import lombok.Data;

/**
 * 操作日志事件
 *
 * <p>由 AOP 切面发布，业务服务监听后异步落库到 sys_operation_log。
 *
 * @author WMS
 */
@Data
@Builder
public class OperationLogEvent {

    /** 操作人 ID */
    private Long userId;

    /** 操作人用户名 */
    private String username;

    /** 操作模块 */
    private String module;

    /** 操作类型（OperationType.code） */
    private String operationType;

    /** 操作描述 */
    private String description;

    /** 请求参数（已脱敏的 JSON） */
    private String requestParams;

    /** 请求 URI */
    private String requestUri;

    /** IP 地址 */
    private String ipAddress;

    /** 是否成功 */
    private boolean success;

    /** 错误信息 */
    private String errorMsg;

    /** 耗时（毫秒） */
    private long costMs;
}
