package com.wms.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存预警记录实体。
 */
@Data
@TableName("inv_warning_record")
public class InvWarningRecord implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 规则 ID */
    private Long ruleId;

    /** SKU ID */
    private Long skuId;

    /** 仓库 ID */
    private Long warehouseId;

    /** 预警类型 LOW/HIGH/EXPIRE/DEAD */
    private String warningType;

    /** 触发时库存 */
    private Integer currentQty;

    /** 阈值 */
    private Integer thresholdValue;

    /** 状态 UNHANDLED/HANDLED/IGNORED */
    private String status;

    /** 触发时间 */
    private LocalDateTime triggerTime;

    /** 处理时间 */
    private LocalDateTime handleTime;

    /** 处理人 */
    private String handleBy;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
