package com.wms.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体（sys_operation_log）
 *
 * @author WMS
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人 ID */
    private Long userId;

    /** 操作人用户名 */
    private String username;

    /** 操作模块 */
    private String module;

    /** 操作类型 */
    private String operationType;

    /** 操作描述 */
    private String description;

    /** 请求参数（已脱敏） */
    private String requestParams;

    /** IP 地址 */
    private String ipAddress;

    /** 是否成功：1 是 0 否 */
    private Integer success;

    /** 错误信息 */
    private String errorMsg;

    /** 操作时间 */
    private LocalDateTime operateTime;
}
