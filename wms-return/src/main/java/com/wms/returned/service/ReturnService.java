package com.wms.returned.service;

import com.wms.common.core.result.PageResult;
import com.wms.returned.domain.entity.ReturnItem;
import com.wms.returned.domain.entity.ReturnOrder;

import java.util.List;

/**
 * 退货服务：创建/收货/质检分级/重新上架/分页。
 */
public interface ReturnService {

    /** 创建退货单（CREATED） */
    Long createReturn(ReturnOrder order, List<ReturnItem> items);

    /** 退货收货：CREATED → RECEIVED */
    void receive(Long id);

    /** 质检分级：RECEIVED → INSPECTED，逐件设置等级 A/B/C */
    void inspect(Long id, List<ReturnItem> gradedItems);

    /** 重新上架：INSPECTED → RESHELVED → CLOSED，A 级回写库存 */
    void reshelve(Long id);

    /** 分页查询退货单 */
    PageResult<ReturnOrder> page(long pageNum, long pageSize);
}
