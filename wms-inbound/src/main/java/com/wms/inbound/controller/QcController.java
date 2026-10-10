package com.wms.inbound.controller;

import com.wms.common.core.result.R;
import com.wms.inbound.domain.entity.InboundQc;
import com.wms.inbound.service.QcService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 入库质检接口。
 */
@RestController
@RequestMapping("/api/v1/inbound/qc")
@RequiredArgsConstructor
public class QcController {

    private final QcService qcService;

    @PostMapping
    public R<Void> record(@RequestBody InboundQc qc) {
        qcService.recordQc(qc);
        return R.ok();
    }

    @PutMapping("/{inboundId}/pass")
    public R<Void> pass(@PathVariable("inboundId") Long inboundId,
                        @RequestParam String operator) {
        qcService.passQc(inboundId, operator);
        return R.ok();
    }
}
