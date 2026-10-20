package com.wms.inventory.service;

import com.wms.common.core.result.PageResult;
import com.wms.inventory.domain.entity.CountItem;
import com.wms.inventory.domain.entity.CountOrder;

import java.util.List;

/**
 * 盘点服务：创建盘点单/录入实盘/生成差异(复用调整单)/分页。
 */
public interface CountService {

    /** 创建盘点单（PENDING），按系统库存回填 systemQty */
    Long createCountOrder(CountOrder order, List<CountItem> items);

    /** 录入实盘数量，回写差异 */
    void recordCount(Long itemId, Integer countedQty);

    /** 生成盘点差异：对差异明细生成并审批调整单，回写库存 */
    void generateDiff(Long countId);

    /** 分页查询盘点单 */
    PageResult<CountOrder> page(long pageNum, long pageSize);
}
