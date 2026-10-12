package com.wms.outbound.service;

import com.wms.common.core.result.PageResult;
import com.wms.outbound.domain.entity.OutboundItem;
import com.wms.outbound.domain.entity.OutboundOrder;

import java.util.List;

/**
 * 出库单服务：订单接入/分配库存(锁定)/取消(释放)/分页。
 */
public interface OutboundService {

    /** 创建出库单（草稿，含明细） */
    Long createOrder(OutboundOrder order, List<OutboundItem> items);

    /** 分配库存：DRAFT → ALLOCATED，锁定库存 */
    void allocate(Long id);

    /** 取消：释放锁定库存并置 CANCELLED */
    void cancel(Long id);

    /** 分页查询出库单 */
    PageResult<OutboundOrder> page(long pageNum, long pageSize);
}
