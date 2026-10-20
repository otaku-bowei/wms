package com.wms.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存调整单明细实体。
 */
@Data
@TableName("inv_adjust_item")
public class InvAdjustItem implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 调整单 ID */
    private Long adjustId;

    /** SKU ID */
    private Long skuId;

    /** 库位 ID */
    private Long locationId;

    /** 批次号 */
    private String batchNo;

    /** 调整前数量 */
    private Integer beforeQty;

    /** 调整后数量 */
    private Integer afterQty;

    /** 差异数量 */
    private Integer diffQty;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
