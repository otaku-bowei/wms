package com.wms.report.controller;

import com.wms.common.core.result.R;
import com.wms.report.domain.dto.CountReportDTO;
import com.wms.report.domain.dto.KpiDTO;
import com.wms.report.domain.dto.StatusCount;
import com.wms.report.domain.dto.WarehouseStock;
import com.wms.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 报表分析接口（KPI 看板 + 业务报表）。
 */
@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/kpi")
    public R<KpiDTO> kpi() {
        return R.ok(reportService.computeKpi());
    }

    @GetMapping("/outbound")
    public R<List<StatusCount>> outbound() {
        return R.ok(reportService.outboundReport());
    }

    @GetMapping("/inbound")
    public R<List<StatusCount>> inbound() {
        return R.ok(reportService.inboundReport());
    }

    @GetMapping("/inventory")
    public R<List<WarehouseStock>> inventory() {
        return R.ok(reportService.inventoryReport());
    }

    @GetMapping("/return")
    public R<List<StatusCount>> returnReport() {
        return R.ok(reportService.returnReport());
    }

    @GetMapping("/count")
    public R<CountReportDTO> count() {
        return R.ok(reportService.countReport());
    }
}
