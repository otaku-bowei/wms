package com.wms.outbound.controller;

import com.wms.common.core.result.PageResult;
import com.wms.common.core.result.R;
import com.wms.outbound.domain.dto.OutboundCreateVO;
import com.wms.outbound.domain.entity.OutboundItem;
import com.wms.outbound.domain.entity.OutboundOrder;
import com.wms.outbound.service.OutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 出库单接口（订单接入/分配库存/取消）。
 */
@RestController
@RequestMapping("/api/v1/outbound/orders")
@RequiredArgsConstructor
public class OutboundController {

    private final OutboundService outboundService;

    @PostMapping
    public R<Long> create(@RequestBody OutboundCreateVO vo) {
        return R.ok(outboundService.createOrder(vo.getOrder(), vo.getItems()));
    }

    @GetMapping
    public R<PageResult<OutboundOrder>> page(@RequestParam(defaultValue = "1") long pageNum,
                                            @RequestParam(defaultValue = "20") long pageSize) {
        return R.ok(outboundService.page(pageNum, pageSize));
    }

    @PutMapping("/{id}/allocate")
    public R<Void> allocate(@PathVariable("id") Long id) {
        outboundService.allocate(id);
        return R.ok();
    }

    @PutMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable("id") Long id) {
        outboundService.cancel(id);
        return R.ok();
    }
}
