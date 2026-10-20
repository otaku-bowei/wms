package com.wms.inventory.service;

import com.wms.common.core.result.PageResult;
import com.wms.inventory.domain.entity.InvAdjust;
import com.wms.inventory.domain.entity.InvAdjustItem;

import java.util.List;

/**
 * 库存调整单服务。
 */
public interface AdjustService {

    /** 创建调整单（草稿） */
    Long createAdjust(InvAdjust adjust, List<InvAdjustItem> items);

    /** 审批调整单：通过则按差异调整库存并记录流水，驳回则置 REJECTED */
    void audit(Long id, boolean approved, String auditor);

    /** 分页查询调整单 */
    PageResult<InvAdjust> page(long pageNum, long pageSize);
}
