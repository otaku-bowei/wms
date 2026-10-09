package com.wms.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 编码规则实体（sys_code_rule）
 *
 * @author WMS
 */
@Data
@TableName("sys_code_rule")
public class SysCodeRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 单据类型 */
    private String ruleType;

    /** 规则名称 */
    private String ruleName;

    /** 前缀 */
    private String prefix;

    /** 日期格式 */
    private String dateFormat;

    /** 流水号位数 */
    private Integer serialLength;

    /** 重置方式：DAY / MONTH / YEAR / NEVER */
    private String serialReset;

    /** 当前流水号 */
    private Integer currentSeq;

    /** 当前日期标识 */
    private String currentDate;

    /** 编码示例 */
    private String sample;

    /** 状态 */
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
