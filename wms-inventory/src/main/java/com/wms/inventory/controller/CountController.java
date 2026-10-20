package com.wms.inventory.controller;

import com.wms.common.core.result.PageResult;
import com.wms.common.core.result.R;
import com.wms.inventory.domain.dto.CountCreateVO;
import com.wms.inventory.domain.entity.CountItem;
import com.wms.inventory.domain.entity.CountOrder;
import com.wms.inventory.service.CountService;
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
 * 盘点接口。
 */
@RestController
@RequestMapping("/api/v1/inventory/count")
@RequiredArgsConstructor
public class CountController {

    private final CountService countService;

    @PostMapping
    public R<Long> create(@RequestBody CountCreateVO vo) {
        return R.ok(countService.createCountOrder(vo.getOrder(), vo.getItems()));
    }

    @PutMapping("/items/{itemId}/record")
    public R<Void> record(@PathVariable("itemId") Long itemId,
                          @RequestParam Integer countedQty) {
        countService.recordCount(itemId, countedQty);
        return R.ok();
    }

    @PostMapping("/{id}/generate")
    public R<Void> generate(@PathVariable("id") Long id) {
        countService.generateDiff(id);
        return R.ok();
    }

    @GetMapping
    public R<PageResult<CountOrder>> page(@RequestParam(defaultValue = "1") long pageNum,
                                         @RequestParam(defaultValue = "20") long pageSize) {
        return R.ok(countService.page(pageNum, pageSize));
    }
}
