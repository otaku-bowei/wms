package com.wms.system.controller;

import com.wms.common.core.result.R;
import com.wms.common.log.annotation.OperationLog;
import com.wms.common.log.enums.OperationType;
import com.wms.common.security.annotation.RequirePermission;
import com.wms.system.domain.entity.SysDict;
import com.wms.system.domain.entity.SysDictItem;
import com.wms.system.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据字典接口
 *
 * @author WMS
 */
@Tag(name = "数据字典", description = "字典类型与字典项查询、缓存刷新")
@RestController
@RequestMapping("/api/v1/config/dicts")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @Operation(summary = "查询字典类型列表")
    @GetMapping
    @RequirePermission("config:view")
    public R<List<SysDict>> dictTypes(@RequestParam(value = "dictType", required = false) String dictType) {
        return R.ok(dictService.listDictTypes(dictType));
    }

    @Operation(summary = "查询字典项")
    @GetMapping("/items")
    @RequirePermission("config:view")
    public R<List<SysDictItem>> dictItems(@RequestParam("dictType") String dictType) {
        return R.ok(dictService.listDictItems(dictType));
    }

    @Operation(summary = "刷新字典缓存")
    @PostMapping("/refresh")
    @RequirePermission("config:edit")
    @OperationLog(module = "CONFIG", type = OperationType.UPDATE, description = "刷新字典缓存")
    public R<Void> refresh() {
        dictService.refreshCache();
        return R.<Void>ok();
    }
}
