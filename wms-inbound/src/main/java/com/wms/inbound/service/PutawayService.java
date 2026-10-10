package com.wms.inbound.service;

import com.wms.inbound.domain.dto.PutawayConfirmVO;
import com.wms.inbound.domain.entity.PutawayTask;

/**
 * 上架任务服务：创建任务 + 上架确认（调用库存服务增加库存）。
 */
public interface PutawayService {

    /** 创建上架任务 */
    Long createTask(PutawayTask task);

    /** 上架确认：调用库存服务增加库存，回写明细上架数量 */
    void confirmPutaway(PutawayConfirmVO vo);
}
