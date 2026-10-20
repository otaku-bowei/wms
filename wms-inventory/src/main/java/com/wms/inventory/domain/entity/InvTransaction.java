package com.wms.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存流水实体（全量变动留痕，不可修改）。
 */
@Data
@TableName("inv_transaction")
public class InvTransaction implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 流水号（唯一） */
    private String transactionNo;

    /** SKU ID */
    private Long skuId;

    /** 仓库 ID */
    private Long warehouseId;

    /** 库位 ID */
    private Long locationId;

    /** 批次号 */
    private String batchNo;

    /** 变动类型 INBOUND/OUTBOUND/ADJUST/LOCK/UNLOCK/FREEZE */
    private String transactionType;

    /** 变动数量（正增负减） */
    private Integer quantity;

    /** 变动前可用库存 */
    private Integer beforeQty;

    /** 变动后可用库存 */
    private Integer afterQty;

    /** 关联单号 */
    private String relatedNo;

    /** 操作人 */
    private String operator;

    /** 操作时间 */
    private LocalDateTime operateTime;

    /** 备注 */
    private String remark;
}
