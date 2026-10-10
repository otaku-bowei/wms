package com.wms.inbound.service;

import com.wms.common.core.result.PageResult;
import com.wms.inbound.domain.entity.InboundItem;
import com.wms.inbound.domain.entity.InboundOrder;

import java.util.List;

/**
 * 入库单服务：预约/确认/到货/提交质检/分页。
 */
public interface InboundService {

    /** 创建入库预约单（草稿，含明细） */
    Long createOrder(InboundOrder order, List<InboundItem> items);

    /** 确认：DRAFT → CONFIRMED */
    void confirm(Long id);

    /** 到货登记：CONFIRMED → ARRIVED */
    void arrive(Long id);

    /** 提交质检：ARRIVED → PENDING_QC */
    void submitQc(Long id);

    /** 分页查询入库单 */
    PageResult<InboundOrder> page(long pageNum, long pageSize);
}
