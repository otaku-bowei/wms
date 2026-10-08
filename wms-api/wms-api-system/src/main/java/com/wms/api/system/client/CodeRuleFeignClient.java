package com.wms.api.system.client;

import com.wms.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * system 服务 - 编码规则内部接口
 *
 * @author WMS
 */
@FeignClient(name = "wms-system", path = "/internal/config")
public interface CodeRuleFeignClient {

    /**
     * 生成下一个编码
     *
     * @param ruleType 单据类型
     * @return 编码（Map 中的 code 字段）
     */
    @GetMapping("/next-code/{ruleType}")
    R<Map<String, String>> nextCode(@PathVariable("ruleType") String ruleType);
}
