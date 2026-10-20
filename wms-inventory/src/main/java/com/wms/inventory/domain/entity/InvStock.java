package com.wms.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 库存实体（SKU × 仓库 × 库位 × 批次）。
 */
@Data
@TableName("inv_stock")
public class InvStock implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** SKU ID */
    private Long skuId;

    /** 仓库 ID */
    private Long warehouseId;

    /** 库位 ID */
    private Long locationId;

    /** 批次号 */
    private String batchNo;

    /** 生产日期 */
    private LocalDate productionDate;

    /** 到期日期 */
    private LocalDate expiryDate;

    /** 总库存 */
    private Integer totalQty;

    /** 可用库存 */
    private Integer availableQty;

    /** 锁定库存 */
    private Integer lockedQty;

    /** 冻结库存 */
    private Integer frozenQty;

    /** 状态 NORMAL/FROZEN */
    private String status;

    /** 乐观锁版本 */
    @Version
    private Long version;

    private java.time.LocalDateTime createTime;
    private java.time.LocalDateTime updateTime;
    private Integer deleted;
}
