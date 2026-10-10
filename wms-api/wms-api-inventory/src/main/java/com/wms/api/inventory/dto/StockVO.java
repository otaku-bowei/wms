package com.wms.api.inventory.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 库存视图对象。
 */
@Data
public class StockVO implements Serializable {

    /** SKU ID */
    private Long skuId;

    /** 库位 ID */
    private Long locationId;

    /** 批次号 */
    private String batchNo;

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
}
