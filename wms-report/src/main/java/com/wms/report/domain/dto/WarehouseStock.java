package com.wms.report.domain.dto;

import lombok.Data;

/**
 * 仓库库存汇总。
 */
@Data
public class WarehouseStock {

    private Long warehouseId;
    private Long totalQty;
    private Long availableQty;

    public WarehouseStock(Long warehouseId, Long totalQty, Long availableQty) {
        this.warehouseId = warehouseId;
        this.totalQty = totalQty;
        this.availableQty = availableQty;
    }
}
