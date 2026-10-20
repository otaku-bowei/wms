package com.wms.inventory.service;

import com.wms.api.inventory.dto.StockChangeDTO;
import com.wms.api.inventory.dto.StockVO;

/**
 * 库存服务：增减/锁定/解锁/查询。
 */
public interface StockService {

    /** 增加库存（上架/入库） */
    void increase(StockChangeDTO dto);

    /** 扣减库存（出库） */
    void decrease(StockChangeDTO dto);

    /** 锁定库存（预占） */
    void lock(StockChangeDTO dto);

    /** 解锁库存（取消预占） */
    void unlock(StockChangeDTO dto);

    /** 按库位汇总库存 */
    StockVO getLocationSummary(Long locationId);
}
