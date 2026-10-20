package com.wms.inventory.service;

import com.wms.common.core.result.PageResult;
import com.wms.inventory.domain.entity.InvWarningRecord;
import com.wms.inventory.domain.entity.InvWarningRule;

import java.util.List;

/**
 * 库存预警服务：规则管理 + 扫描生成预警记录。
 */
public interface WarningService {

    /** 创建预警规则 */
    Long createRule(InvWarningRule rule);

    /** 更新预警规则 */
    void updateRule(Long id, InvWarningRule rule);

    /** 分页查询规则 */
    PageResult<InvWarningRule> pageRules(long pageNum, long pageSize);

    /** 扫描库存，按启用规则生成预警记录 */
    int checkAndGenerate();

    /** 查询预警记录 */
    List<InvWarningRecord> listRecords();
}
