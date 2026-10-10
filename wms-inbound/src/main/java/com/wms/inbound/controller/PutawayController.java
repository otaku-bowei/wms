package com.wms.inbound.controller;

import com.wms.common.core.result.R;
import com.wms.inbound.domain.dto.PutawayConfirmVO;
import com.wms.inbound.domain.entity.PutawayTask;
import com.wms.inbound.service.PutawayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 上架任务接口。
 */
@RestController
@RequestMapping("/api/v1/inbound/putaway")
@RequiredArgsConstructor
public class PutawayController {

    private final PutawayService putawayService;

    @PostMapping
    public R<Long> createTask(@RequestBody PutawayTask task) {
        return R.ok(putawayService.createTask(task));
    }

    @PutMapping("/confirm")
    public R<Void> confirm(@RequestBody PutawayConfirmVO vo) {
        putawayService.confirmPutaway(vo);
        return R.ok();
    }
}
