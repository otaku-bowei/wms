package com.wms.system.service;

import com.wms.system.domain.entity.SysCodeRule;

import java.util.List;

/**
 * 编码规则服务接口
 *
 * @author WMS
 */
public interface CodeRuleService {

    /**
     * 查询全部编码规则
     *
     * @return 规则列表
     */
    List<SysCodeRule> listRules();

    /**
     * 修改编码规则
     *
     * @param id       规则 ID
     * @param prefix        前缀
     * @param dateFormat    日期格式
     * @param serialLength  流水号位数
     * @param serialReset   重置方式
     */
    void updateRule(Long id, String prefix, String dateFormat, Integer serialLength, String serialReset);

    /**
     * 生成下一个编码
     *
     * @param ruleType 单据类型
     * @return 生成的编码
     */
    String generateNextCode(String ruleType);
}
