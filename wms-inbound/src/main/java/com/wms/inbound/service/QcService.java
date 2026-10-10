package com.wms.inbound.service;

import com.wms.inbound.domain.entity.InboundQc;

/**
 * 入库质检服务：质检记录 + 质检通过生成上架任务。
 */
public interface QcService {

    /** 登记质检记录（同步更新明细的实收/合格/不合格数量） */
    void recordQc(InboundQc qc);

    /** 质检通过：PENDING_QC → PUTTING_AWAY，并生成上架任务 */
    void passQc(Long inboundId, String operator);
}
