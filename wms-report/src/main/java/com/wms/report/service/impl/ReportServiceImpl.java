package com.wms.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.report.domain.dto.CountReportDTO;
import com.wms.report.domain.dto.KpiDTO;
import com.wms.report.domain.dto.StatusCount;
import com.wms.report.domain.dto.WarehouseStock;
import com.wms.report.domain.entity.ReportCountItem;
import com.wms.report.domain.entity.ReportInboundOrder;
import com.wms.report.domain.entity.ReportInvStock;
import com.wms.report.domain.entity.ReportOutboundOrder;
import com.wms.report.domain.entity.ReportReturnOrder;
import com.wms.report.mapper.ReportCountItemMapper;
import com.wms.report.mapper.ReportInboundOrderMapper;
import com.wms.report.mapper.ReportInvStockMapper;
import com.wms.report.mapper.ReportOutboundOrderMapper;
import com.wms.report.mapper.ReportReturnOrderMapper;
import com.wms.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报表分析服务实现：读取共享库各业务表做聚合。
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportInvStockMapper stockMapper;
    private final ReportOutboundOrderMapper outboundMapper;
    private final ReportInboundOrderMapper inboundMapper;
    private final ReportCountItemMapper countItemMapper;
    private final ReportReturnOrderMapper returnMapper;

    @Override
    public KpiDTO computeKpi() {
        KpiDTO kpi = new KpiDTO();

        List<ReportInvStock> stocks = stockMapper.selectList(new LambdaQueryWrapper<ReportInvStock>());
        kpi.setTotalStock(stocks.stream().mapToLong(s -> orZero(s.getTotalQty())).sum());
        kpi.setAvailableStock(stocks.stream().mapToLong(s -> orZero(s.getAvailableQty())).sum());

        List<ReportCountItem> countItems = countItemMapper.selectList(
                new LambdaQueryWrapper<ReportCountItem>().eq(ReportCountItem::getStatus, "ADJUSTED"));
        long total = countItems.size();
        long diff = countItems.stream().filter(c -> orZero(c.getDiffQty()) != 0).count();
        kpi.setInventoryAccuracy(total == 0 ? 100.0 : (total - diff) * 100.0 / total);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<ReportInboundOrder> inbound = inboundMapper.selectList(new LambdaQueryWrapper<>());
        kpi.setTodayInbound(inbound.stream()
                .filter(i -> i.getCreateTime() != null && !i.getCreateTime().isBefore(startOfDay)).count());

        List<ReportOutboundOrder> outbound = outboundMapper.selectList(new LambdaQueryWrapper<>());
        kpi.setTodayOutbound(outbound.stream()
                .filter(o -> o.getCreateTime() != null && !o.getCreateTime().isBefore(startOfDay)).count());
        kpi.setPendingOutbound(outbound.stream()
                .filter(o -> !"SHIPPED".equals(o.getStatus()) && !"CANCELLED".equals(o.getStatus())).count());

        List<ReportReturnOrder> returns = returnMapper.selectList(new LambdaQueryWrapper<>());
        kpi.setTodayReturn(returns.stream()
                .filter(r -> r.getCreateTime() != null && !r.getCreateTime().isBefore(startOfDay)).count());

        return kpi;
    }

    @Override
    public List<StatusCount> outboundReport() {
        return statusCount(outboundMapper.selectList(new LambdaQueryWrapper<>()), ReportOutboundOrder::getStatus);
    }

    @Override
    public List<StatusCount> inboundReport() {
        return statusCount(inboundMapper.selectList(new LambdaQueryWrapper<>()), ReportInboundOrder::getStatus);
    }

    @Override
    public List<WarehouseStock> inventoryReport() {
        List<ReportInvStock> stocks = stockMapper.selectList(new LambdaQueryWrapper<>());
        Map<Long, WarehouseStock> map = new LinkedHashMap<>();
        for (ReportInvStock s : stocks) {
            WarehouseStock ws = map.computeIfAbsent(s.getWarehouseId(), k -> new WarehouseStock(k, 0L, 0L));
            ws.setTotalQty(ws.getTotalQty() + orZero(s.getTotalQty()));
            ws.setAvailableQty(ws.getAvailableQty() + orZero(s.getAvailableQty()));
        }
        return new java.util.ArrayList<>(map.values());
    }

    @Override
    public List<StatusCount> returnReport() {
        return statusCount(returnMapper.selectList(new LambdaQueryWrapper<>()), ReportReturnOrder::getStatus);
    }

    @Override
    public CountReportDTO countReport() {
        List<ReportCountItem> items = countItemMapper.selectList(
                new LambdaQueryWrapper<ReportCountItem>().eq(ReportCountItem::getStatus, "ADJUSTED"));
        CountReportDTO dto = new CountReportDTO();
        long total = items.size();
        long diff = items.stream().filter(c -> orZero(c.getDiffQty()) != 0).count();
        dto.setTotalItems((long) total);
        dto.setDiffItems(diff);
        dto.setAdjustedItems((long) total);
        dto.setAccuracy(total == 0 ? 100.0 : (total - diff) * 100.0 / total);
        return dto;
    }

    private <T> List<StatusCount> statusCount(List<T> rows, java.util.function.Function<T, String> statusFn) {
        Map<String, Long> counts = rows.stream()
                .collect(Collectors.groupingBy(statusFn, Collectors.counting()));
        return counts.entrySet().stream()
                .map(e -> new StatusCount(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private int orZero(Integer v) {
        return v == null ? 0 : v;
    }
}
