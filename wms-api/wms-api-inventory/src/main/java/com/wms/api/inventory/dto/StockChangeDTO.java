package com.wms.api.inventory.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 库存变动 DTO（跨服务传递）。
 */
@Data
public class StockChangeDTO implements Serializable {

    /** SKU ID */
    private Long skuId;

    /** 库位 ID */
    private Long locationId;

    /** 批次号 */
    private String batchNo;

    /** 变动数量（正数增加，负数扣减/锁定） */
    private Integer quantity;

    /** 关联单号（入库单号/上架任务号/出库单号） */
    private String relatedNo;

    /** 操作人 */
    private String operator;

    /** 备注 */
    private String remark;
}
