package com.wms.outbound.service;

import com.wms.common.core.result.PageResult;
import com.wms.outbound.domain.dto.PickConfirmVO;
import com.wms.outbound.domain.entity.PickTask;

/**
 * 拣货服务：按波次生成拣货任务/拣货确认/分页。
 */
public interface PickService {

    /** 按波次为明细生成拣货任务 */
    void createTasks(Long waveId);

    /** 拣货确认：回写已拣数量，全部完成则置订单 PICKED */
    void confirmPick(PickConfirmVO vo);

    /** 分页查询拣货任务 */
    PageResult<PickTask> page(long pageNum, long pageSize);
}
