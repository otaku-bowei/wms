package com.wms.outbound.controller;

import com.wms.common.core.result.PageResult;
import com.wms.common.core.result.R;
import com.wms.outbound.domain.dto.PickConfirmVO;
import com.wms.outbound.domain.entity.PickTask;
import com.wms.outbound.service.PickService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 拣货接口（生成任务/拣货确认/分页）。
 */
@RestController
@RequestMapping("/api/v1/outbound/pick")
@RequiredArgsConstructor
public class PickController {

    private final PickService pickService;

    @PostMapping("/tasks")
    public R<Void> createTasks(@RequestParam Long waveId) {
        pickService.createTasks(waveId);
        return R.ok();
    }

    @PutMapping("/confirm")
    public R<Void> confirm(@RequestBody PickConfirmVO vo) {
        pickService.confirmPick(vo);
        return R.ok();
    }

    @GetMapping
    public R<PageResult<PickTask>> page(@RequestParam(defaultValue = "1") long pageNum,
                                       @RequestParam(defaultValue = "20") long pageSize) {
        return R.ok(pickService.page(pageNum, pageSize));
    }
}
