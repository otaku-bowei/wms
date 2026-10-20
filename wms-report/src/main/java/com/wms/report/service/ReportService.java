package com.wms.report.service;

import com.wms.report.domain.dto.CountReportDTO;
import com.wms.report.domain.dto.KpiDTO;
import com.wms.report.domain.dto.StatusCount;
import com.wms.report.domain.dto.WarehouseStock;

import java.util.List;

/**
 * 报表分析服务：KPI 看板 + 五类业务报表。
 */
public interface ReportService {

    /** KPI 实时看板 */
    KpiDTO computeKpi();

    /** 出库报表：按状态分布 */
    List<StatusCount> outboundReport();

    /** 入库报表：按状态分布 */
    List<StatusCount> inboundReport();

    /** 库存报表：按仓库汇总 */
    List<WarehouseStock> inventoryReport();

    /** 退货报表：按状态分布 */
    List<StatusCount> returnReport();

    /** 盘点报表：账实准确率 */
    CountReportDTO countReport();
}
