package com.wms.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统参数实体（sys_config）
 *
 * @author WMS
 */
@Data
@TableName("sys_config")
public class SysConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 参数键 */
    private String paramKey;

    /** 参数名称 */
    private String paramName;

    /** 参数值 */
    private String paramValue;

    /** 默认值 */
    private String defaultValue;

    /** 分组：SECURITY / BUSINESS / SYSTEM */
    private String paramGroup;

    /** 是否需重启：1 是 0 否 */
    private Integer needRestart;

    /** 说明 */
    private String remark;

    private String updateBy;
    private LocalDateTime updateTime;
}
