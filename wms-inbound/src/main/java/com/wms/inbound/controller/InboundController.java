package com.wms.inbound.controller;

import com.wms.common.core.result.PageResult;
import com.wms.common.core.result.R;
import com.wms.inbound.domain.dto.InboundCreateVO;
import com.wms.inbound.domain.entity.InboundItem;
import com.wms.inbound.domain.entity.InboundOrder;
import com.wms.inbound.service.InboundService;
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
 * 入库单接口（预约/确认/到货/提交质检）。
 */
@RestController
@RequestMapping("/api/v1/inbound/orders")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;

    @PostMapping
    public R<Long> create(@RequestBody InboundCreateVO vo) {
        return R.ok(inboundService.createOrder(vo.getOrder(), vo.getItems()));
    }

    @GetMapping
    public R<PageResult<InboundOrder>> page(@RequestParam(defaultValue = "1") long pageNum,
                                            @RequestParam(defaultValue = "20") long pageSize) {
        return R.ok(inboundService.page(pageNum, pageSize));
    }

    @PutMapping("/{id}/confirm")
    public R<Void> confirm(@PathVariable("id") Long id) {
        inboundService.confirm(id);
        return R.ok();
    }

    @PutMapping("/{id}/arrive")
    public R<Void> arrive(@PathVariable("id") Long id) {
        inboundService.arrive(id);
        return R.ok();
    }

    @PutMapping("/{id}/submit-qc")
    public R<Void> submitQc(@PathVariable("id") Long id) {
        inboundService.submitQc(id);
        return R.ok();
    }
}
