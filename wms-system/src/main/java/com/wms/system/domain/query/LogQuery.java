package com.wms.system.domain.query;

import com.wms.common.core.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 日志查询条件（操作日志 / 登录日志共用）
 *
 * @author WMS
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LogQuery extends PageQuery {

    /** 操作人 / 用户名 */
    private String username;

    /** 操作模块 */
    private String module;

    /** 操作类型 */
    private String operationType;

    /** 登录结果：SUCCESS / FAIL / LOCKED */
    private String loginResult;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;
}
