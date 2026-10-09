package com.wms.system.controller;

import com.wms.common.core.result.PageResult;
import com.wms.common.core.result.R;
import com.wms.common.security.annotation.RequirePermission;
import com.wms.system.domain.entity.SysLoginLog;
import com.wms.system.domain.entity.SysOperationLog;
import com.wms.system.domain.query.LogQuery;
import com.wms.system.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志接口
 *
 * @author WMS
 */
@Tag(name = "日志中心", description = "操作日志与登录日志查询")
@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @Operation(summary = "分页查询操作日志")
    @GetMapping("/operation")
    @RequirePermission("log:view")
    public R<PageResult<SysOperationLog>> operationLogs(LogQuery query) {
        return R.ok(logService.pageOperationLogs(query));
    }

    @Operation(summary = "分页查询登录日志")
    @GetMapping("/login")
    @RequirePermission("log:view")
    public R<PageResult<SysLoginLog>> loginLogs(LogQuery query) {
        return R.ok(logService.pageLoginLogs(query));
    }
}
