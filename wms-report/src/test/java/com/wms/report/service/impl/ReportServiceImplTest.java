package com.wms.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.wms.report.domain.dto.CountReportDTO;
import com.wms.report.domain.dto.KpiDTO;
import com.wms.report.domain.dto.StatusCount;
import com.wms.report.domain.dto.WarehouseStock;
import com.wms.report.domain.entity.ReportCountItem;
import com.wms.report.domain.entity.ReportInboundOrder;
import com.wms.report.domain.entity.ReportInvStock;
import com.wms.report.domain.entity.ReportOutboundOrder;
import com.wms.report.mapper.ReportCountItemMapper;
import com.wms.report.mapper.ReportInboundOrderMapper;
import com.wms.report.mapper.ReportInvStockMapper;
import com.wms.report.mapper.ReportOutboundOrderMapper;
import com.wms.report.mapper.ReportReturnOrderMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 报表服务单元测试：KPI 聚合、出库状态分布、库存按仓库汇总、盘点准确率。
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportInvStockMapper stockMapper;

    @Mock
    private ReportOutboundOrderMapper outboundMapper;

    @Mock
    private ReportInboundOrderMapper inboundMapper;

    @Mock
    private ReportCountItemMapper countItemMapper;

    @Mock
    private ReportReturnOrderMapper returnMapper;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    @DisplayName("KPI：库存总量/准确率/今日单量聚合正确")
    void computeKpi_shouldAggregate() {
        ReportInvStock s1 = new ReportInvStock();
        s1.setTotalQty(100);
        s1.setAvailableQty(80);
        ReportInvStock s2 = new ReportInvStock();
        s2.setTotalQty(50);
        s2.setAvailableQty(40);
        when(stockMapper.selectList(any(Wrapper.class))).thenReturn(List.of(s1, s2));

        LocalDateTime today = LocalDateTime.now();
        ReportOutboundOrder o1 = new ReportOutboundOrder();
        o1.setStatus("PICKING");
        o1.setCreateTime(today);
        ReportOutboundOrder o2 = new ReportOutboundOrder();
        o2.setStatus("SHIPPED");
        o2.setCreateTime(today.minusDays(5));
        when(outboundMapper.selectList(any(Wrapper.class))).thenReturn(List.of(o1, o2));

        ReportInboundOrder i1 = new ReportInboundOrder();
        i1.setStatus("COMPLETED");
        i1.setCreateTime(today);
        when(inboundMapper.selectList(any(Wrapper.class))).thenReturn(List.of(i1));

        ReportCountItem c1 = new ReportCountItem();
        c1.setStatus("ADJUSTED");
        c1.setDiffQty(0);
        ReportCountItem c2 = new ReportCountItem();
        c2.setStatus("ADJUSTED");
        c2.setDiffQty(3);
        when(countItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(c1, c2));

        when(returnMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        KpiDTO kpi = reportService.computeKpi();

        assertThat(kpi.getTotalStock()).isEqualTo(150L);
        assertThat(kpi.getAvailableStock()).isEqualTo(120L);
        assertThat(kpi.getInventoryAccuracy()).isEqualTo(50.0);
        assertThat(kpi.getTodayInbound()).isEqualTo(1L);
        assertThat(kpi.getTodayOutbound()).isEqualTo(1L);
        assertThat(kpi.getPendingOutbound()).isEqualTo(1L); // o1 PICKING 未发货
    }

    @Test
    @DisplayName("出库报表：按状态分布计数")
    void outboundReport_shouldGroupByStatus() {
        ReportOutboundOrder o1 = new ReportOutboundOrder();
        o1.setStatus("PICKING");
        ReportOutboundOrder o2 = new ReportOutboundOrder();
        o2.setStatus("PICKING");
        ReportOutboundOrder o3 = new ReportOutboundOrder();
        o3.setStatus("SHIPPED");
        when(outboundMapper.selectList(any(Wrapper.class))).thenReturn(List.of(o1, o2, o3));

        List<StatusCount> result = reportService.outboundReport();
        assertThat(result).anyMatch(s -> "PICKING".equals(s.getStatus()) && s.getCount() == 2L);
        assertThat(result).anyMatch(s -> "SHIPPED".equals(s.getStatus()) && s.getCount() == 1L);
    }

    @Test
    @DisplayName("库存报表：按仓库汇总总量与可用量")
    void inventoryReport_shouldSumByWarehouse() {
        ReportInvStock s1 = new ReportInvStock();
        s1.setWarehouseId(1L);
        s1.setTotalQty(100);
        s1.setAvailableQty(80);
        ReportInvStock s2 = new ReportInvStock();
        s2.setWarehouseId(1L);
        s2.setTotalQty(50);
        s2.setAvailableQty(40);
        when(stockMapper.selectList(any(Wrapper.class))).thenReturn(List.of(s1, s2));

        List<WarehouseStock> result = reportService.inventoryReport();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWarehouseId()).isEqualTo(1L);
        assertThat(result.get(0).getTotalQty()).isEqualTo(150L);
        assertThat(result.get(0).getAvailableQty()).isEqualTo(120L);
    }

    @Test
    @DisplayName("盘点报表：账实准确率计算")
    void countReport_shouldComputeAccuracy() {
        ReportCountItem c1 = new ReportCountItem();
        c1.setStatus("ADJUSTED");
        c1.setDiffQty(0);
        ReportCountItem c2 = new ReportCountItem();
        c2.setStatus("ADJUSTED");
        c2.setDiffQty(5);
        ReportCountItem c3 = new ReportCountItem();
        c3.setStatus("ADJUSTED");
        c3.setDiffQty(2);
        when(countItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(c1, c2, c3));

        CountReportDTO dto = reportService.countReport();
        assertThat(dto.getTotalItems()).isEqualTo(3L);
        assertThat(dto.getDiffItems()).isEqualTo(2L);
        assertThat(dto.getAccuracy()).isEqualTo((3.0 - 2) * 100.0 / 3);
    }
}
