package com.wms.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存预警规则实体。
 */
@Data
@TableName("inv_warning_rule")
public class InvWarningRule implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** SKU ID（空表示全仓） */
    private Long skuId;

    /** 仓库 ID */
    private Long warehouseId;

    /** 预警类型 LOW/HIGH/EXPIRE/DEAD */
    private String warningType;

    /** 低库存阈值 */
    private Integer minQty;

    /** 高库存阈值 */
    private Integer maxQty;

    /** 临期天数 */
    private Integer expireDays;

    /** 呆滞天数 */
    private Integer deadDays;

    /** 是否启用 1/0 */
    private Integer enabled;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
