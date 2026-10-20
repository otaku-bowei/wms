package com.wms.report.domain.dto;

import lombok.Data;

/**
 * KPI 看板数据。
 */
@Data
public class KpiDTO {

    /** 库存账实准确率(%) */
    private Double inventoryAccuracy;

    /** 库存总量 */
    private Long totalStock;

    /** 可用库存总量 */
    private Long availableStock;

    /** 今日入库单数 */
    private Long todayInbound;

    /** 今日出库单数 */
    private Long todayOutbound;

    /** 今日退货单数 */
    private Long todayReturn;

    /** 待出库（未发货）单数 */
    private Long pendingOutbound;
}
