package com.wms.outbound.controller;

import com.wms.common.core.result.PageResult;
import com.wms.common.core.result.R;
import com.wms.outbound.domain.dto.WaveGenerateVO;
import com.wms.outbound.domain.entity.WaveOrder;
import com.wms.outbound.service.WaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 波次接口（创建/生成/分页）。
 */
@RestController
@RequestMapping("/api/v1/outbound/waves")
@RequiredArgsConstructor
public class WaveController {

    private final WaveService waveService;

    @PostMapping
    public R<Long> create(@RequestBody WaveOrder wave) {
        return R.ok(waveService.createWave(wave));
    }

    @PostMapping("/generate")
    public R<Void> generate(@RequestBody WaveGenerateVO vo) {
        waveService.generate(vo);
        return R.ok();
    }

    @GetMapping
    public R<PageResult<WaveOrder>> page(@RequestParam(defaultValue = "1") long pageNum,
                                        @RequestParam(defaultValue = "20") long pageSize) {
        return R.ok(waveService.page(pageNum, pageSize));
    }
}
