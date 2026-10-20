package com.wms.inventory.controller;

import com.wms.common.core.result.PageResult;
import com.wms.common.core.result.R;
import com.wms.inventory.domain.dto.AdjustCreateVO;
import com.wms.inventory.domain.entity.InvAdjust;
import com.wms.inventory.service.AdjustService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存调整单接口。
 */
@RestController
@RequestMapping("/api/v1/inventory/adjusts")
@RequiredArgsConstructor
public class AdjustController {

    private final AdjustService adjustService;

    @PostMapping
    public R<Long> create(@RequestBody AdjustCreateVO vo) {
        return R.ok(adjustService.createAdjust(vo.getAdjust(), vo.getItems()));
    }

    @GetMapping
    public R<PageResult<InvAdjust>> page(@RequestParam(defaultValue = "1") long pageNum,
                                         @RequestParam(defaultValue = "20") long pageSize) {
        return R.ok(adjustService.page(pageNum, pageSize));
    }

    @PutMapping("/{id}/audit")
    public R<Void> audit(@PathVariable("id") Long id,
                         @RequestParam boolean approved,
                         @RequestParam String auditor) {
        adjustService.audit(id, approved, auditor);
        return R.ok();
    }
}
