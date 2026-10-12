package com.wms.outbound.service;

import com.wms.common.core.result.PageResult;
import com.wms.outbound.domain.dto.WaveGenerateVO;
import com.wms.outbound.domain.entity.WaveOrder;

/**
 * 波次服务：创建波次/生成波次(纳入出库单并锁定库存)/分页。
 */
public interface WaveService {

    /** 创建波次（PENDING） */
    Long createWave(WaveOrder wave);

    /** 生成波次：纳入出库单，分配库存并置 ALLOCATED */
    void generate(WaveGenerateVO vo);

    /** 分页查询波次 */
    PageResult<WaveOrder> page(long pageNum, long pageSize);
}
