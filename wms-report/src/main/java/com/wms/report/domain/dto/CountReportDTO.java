package com.wms.report.domain.dto;

import lombok.Data;

/**
 * 盘点报表（账实准确率）。
 */
@Data
public class CountReportDTO {

    /** 账实准确率(%) */
    private Double accuracy;

    /** 已复盘明细总数 */
    private Long totalItems;

    /** 差异明细数 */
    private Long diffItems;

    /** 已调整明细数 */
    private Long adjustedItems;
}
