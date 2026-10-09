package com.wms.system.controller;

import com.wms.common.core.result.R;
import com.wms.common.log.annotation.OperationLog;
import com.wms.common.log.enums.OperationType;
import com.wms.common.security.annotation.RequirePermission;
import com.wms.system.domain.entity.SysCodeRule;
import com.wms.system.service.CodeRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 编码规则接口
 *
 * @author WMS
 */
@Tag(name = "编码规则", description = "单据编码规则查询、修改与编码生成")
@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class CodeRuleController {

    private final CodeRuleService codeRuleService;

    @Operation(summary = "查询编码规则列表")
    @GetMapping("/code-rules")
    @RequirePermission("config:view")
    public R<List<SysCodeRule>> list() {
        return R.ok(codeRuleService.listRules());
    }

    @Operation(summary = "修改编码规则")
    @PutMapping("/code-rules/{id}")
    @RequirePermission("config:edit")
    @OperationLog(module = "CONFIG", type = OperationType.UPDATE, description = "修改编码规则")
    public R<Void> update(@PathVariable("id") Long id,
                          @RequestParam(value = "prefix", required = false) String prefix,
                          @RequestParam(value = "dateFormat", required = false) String dateFormat,
                          @RequestParam(value = "serialLength", required = false) Integer serialLength,
                          @RequestParam(value = "serialReset", required = false) String serialReset) {
        codeRuleService.updateRule(id, prefix, dateFormat, serialLength, serialReset);
        return R.<Void>ok();
    }

    @Operation(summary = "生成下一个编码")
    @GetMapping("/next-code/{ruleType}")
    @RequirePermission("config:view")
    public R<Map<String, String>> nextCode(@PathVariable("ruleType") String ruleType) {
        String code = codeRuleService.generateNextCode(ruleType);
        Map<String, String> data = new HashMap<>(2);
        data.put("code", code);
        return R.ok(data);
    }
}
