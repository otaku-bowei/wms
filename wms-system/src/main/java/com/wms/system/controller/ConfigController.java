package com.wms.system.controller;

import com.wms.common.core.result.R;
import com.wms.common.log.annotation.OperationLog;
import com.wms.common.log.enums.OperationType;
import com.wms.common.security.annotation.RequirePermission;
import com.wms.system.domain.dto.ConfigUpdateDTO;
import com.wms.system.domain.entity.SysConfig;
import com.wms.system.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统参数接口
 *
 * @author WMS
 */
@Tag(name = "系统参数", description = "系统参数查询与修改")
@RestController
@RequestMapping("/api/v1/config/params")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    @Operation(summary = "查询系统参数列表")
    @GetMapping
    @RequirePermission("config:view")
    public R<List<SysConfig>> list(@RequestParam(value = "paramGroup", required = false) String paramGroup) {
        return R.ok(configService.listParams(paramGroup));
    }

    @Operation(summary = "批量修改系统参数")
    @PutMapping
    @RequirePermission("config:edit")
    @OperationLog(module = "CONFIG", type = OperationType.UPDATE, description = "修改系统参数")
    public R<Void> update(@Valid @RequestBody List<ConfigUpdateDTO> params) {
        Map<String, String> paramMap = new HashMap<>(params.size());
        for (ConfigUpdateDTO item : params) {
            paramMap.put(item.getParamKey(), item.getParamValue());
        }
        configService.updateParams(paramMap);
        return R.<Void>ok();
    }
}
