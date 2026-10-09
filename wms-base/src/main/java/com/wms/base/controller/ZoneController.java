package com.wms.base.controller;

import com.wms.base.domain.dto.ZoneDTO;
import com.wms.base.service.WarehouseService;
import com.wms.common.core.result.R;
import com.wms.common.log.annotation.OperationLog;
import com.wms.common.log.enums.OperationType;
import com.wms.common.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 区域管理接口
 *
 * @author WMS
 */
@Tag(name = "区域管理", description = "仓库区域修改")
@RestController
@RequestMapping("/api/v1/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final WarehouseService warehouseService;

    @Operation(summary = "修改区域")
    @PutMapping("/{id}")
    @RequirePermission("warehouse:edit")
    @OperationLog(module = "WAREHOUSE", type = OperationType.UPDATE, description = "修改仓库区域")
    public R<Void> update(@PathVariable("id") Long id, @Valid @RequestBody ZoneDTO dto) {
        warehouseService.updateZone(id, dto);
        return R.<Void>ok();
    }
}
