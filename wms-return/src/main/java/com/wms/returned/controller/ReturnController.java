package com.wms.returned.controller;

import com.wms.common.core.result.PageResult;
import com.wms.common.core.result.R;
import com.wms.returned.domain.dto.ReturnCreateVO;
import com.wms.returned.domain.entity.ReturnItem;
import com.wms.returned.domain.entity.ReturnOrder;
import com.wms.returned.service.ReturnService;
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
 * 退货接口（收货/质检/重新上架）。
 */
@RestController
@RequestMapping("/api/v1/return")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    @PostMapping
    public R<Long> create(@RequestBody ReturnCreateVO vo) {
        return R.ok(returnService.createReturn(vo.getOrder(), vo.getItems()));
    }

    @PutMapping("/{id}/receive")
    public R<Void> receive(@PathVariable("id") Long id) {
        returnService.receive(id);
        return R.ok();
    }

    @PutMapping("/{id}/inspect")
    public R<Void> inspect(@PathVariable("id") Long id,
                           @RequestBody List<ReturnItem> gradedItems) {
        returnService.inspect(id, gradedItems);
        return R.ok();
    }

    @PutMapping("/{id}/reshelve")
    public R<Void> reshelve(@PathVariable("id") Long id) {
        returnService.reshelve(id);
        return R.ok();
    }

    @GetMapping
    public R<PageResult<ReturnOrder>> page(@RequestParam(defaultValue = "1") long pageNum,
                                           @RequestParam(defaultValue = "20") long pageSize) {
        return R.ok(returnService.page(pageNum, pageSize));
    }
}
