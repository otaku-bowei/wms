package com.wms.outbound.controller;

import com.wms.common.core.result.R;
import com.wms.outbound.domain.dto.PackVO;
import com.wms.outbound.domain.dto.ShipVO;
import com.wms.outbound.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 复核打包与装车接口。
 */
@RestController
@RequestMapping("/api/v1/outbound")
@RequiredArgsConstructor
public class PackageController {

    private final PackageService packageService;

    @PostMapping("/pack")
    public R<Long> pack(@RequestBody PackVO vo) {
        return R.ok(packageService.pack(vo));
    }

    @PostMapping("/ship")
    public R<Void> ship(@RequestBody ShipVO vo) {
        packageService.ship(vo);
        return R.ok();
    }
}
