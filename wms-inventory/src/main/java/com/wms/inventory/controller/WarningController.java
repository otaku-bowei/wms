package com.wms.inventory.controller;

import com.wms.common.core.result.PageResult;
import com.wms.common.core.result.R;
import com.wms.inventory.domain.entity.InvWarningRecord;
import com.wms.inventory.domain.entity.InvWarningRule;
import com.wms.inventory.service.WarningService;
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
 * 库存预警接口。
 */
@RestController
@RequestMapping("/api/v1/inventory/warnings")
@RequiredArgsConstructor
public class WarningController {

    private final WarningService warningService;

    @PostMapping("/rules")
    public R<Long> createRule(@RequestBody InvWarningRule rule) {
        return R.ok(warningService.createRule(rule));
    }

    @PutMapping("/rules/{id}")
    public R<Void> updateRule(@PathVariable("id") Long id, @RequestBody InvWarningRule rule) {
        warningService.updateRule(id, rule);
        return R.ok();
    }

    @GetMapping("/rules")
    public R<PageResult<InvWarningRule>> pageRules(@RequestParam(defaultValue = "1") long pageNum,
                                                   @RequestParam(defaultValue = "20") long pageSize) {
        return R.ok(warningService.pageRules(pageNum, pageSize));
    }

    @PostMapping("/check")
    public R<Integer> check() {
        return R.ok(warningService.checkAndGenerate());
    }

    @GetMapping("/records")
    public R<List<InvWarningRecord>> records() {
        return R.ok(warningService.listRecords());
    }
}
