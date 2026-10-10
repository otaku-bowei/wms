package com.wms.inbound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.inbound.domain.InboundStatus;
import com.wms.inbound.domain.entity.InboundItem;
import com.wms.inbound.domain.entity.InboundOrder;
import com.wms.inbound.domain.entity.InboundQc;
import com.wms.inbound.domain.entity.PutawayTask;
import com.wms.inbound.mapper.InboundItemMapper;
import com.wms.inbound.mapper.InboundOrderMapper;
import com.wms.inbound.mapper.InboundQcMapper;
import com.wms.inbound.mapper.PutawayTaskMapper;
import com.wms.inbound.service.QcService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库质检服务实现：质检记录 + 质检通过生成上架任务。
 */
@Service
@RequiredArgsConstructor
public class QcServiceImpl implements QcService {

    private final InboundOrderMapper inboundOrderMapper;
    private final InboundItemMapper inboundItemMapper;
    private final InboundQcMapper inboundQcMapper;
    private final PutawayTaskMapper putawayTaskMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordQc(InboundQc qc) {
        InboundOrder order = inboundOrderMapper.selectById(qc.getInboundId());
        if (order == null || !InboundStatus.PENDING_QC.equals(order.getStatus())) {
            throw new BizException(ErrorCode.INBOUND_STATUS_INVALID);
        }
        InboundItem item = inboundItemMapper.selectById(qc.getItemId());
        if (item == null || !qc.getInboundId().equals(item.getInboundId())) {
            throw new BizException(ErrorCode.INBOUND_NOT_FOUND);
        }
        item.setReceivedQty(orZero(qc.getInspectQty()));
        item.setQualifiedQty(orZero(qc.getQualifiedQty()));
        item.setDefectQty(orZero(qc.getDefectQty()));
        item.setUpdateTime(LocalDateTime.now());
        inboundItemMapper.updateById(item);

        qc.setSkuId(item.getSkuId());
        qc.setBatchNo(item.getBatchNo());
        qc.setInspectTime(qc.getInspectTime() == null ? LocalDateTime.now() : qc.getInspectTime());
        qc.setCreateTime(LocalDateTime.now());
        qc.setUpdateTime(LocalDateTime.now());
        qc.setDeleted(0);
        inboundQcMapper.insert(qc);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void passQc(Long inboundId, String operator) {
        InboundOrder order = inboundOrderMapper.selectById(inboundId);
        if (order == null) {
            throw new BizException(ErrorCode.INBOUND_NOT_FOUND);
        }
        if (!InboundStatus.PENDING_QC.equals(order.getStatus())) {
            throw new BizException(ErrorCode.INBOUND_STATUS_INVALID);
        }
        long existed = putawayTaskMapper.selectCount(new LambdaQueryWrapper<PutawayTask>()
                .eq(PutawayTask::getInboundId, inboundId).eq(PutawayTask::getDeleted, 0));
        if (existed > 0) {
            throw new BizException(ErrorCode.INBOUND_STATUS_INVALID);
        }
        List<InboundItem> items = inboundItemMapper.selectList(new LambdaQueryWrapper<InboundItem>()
                .eq(InboundItem::getInboundId, inboundId).eq(InboundItem::getDeleted, 0));
        for (InboundItem item : items) {
            int plan = orZero(item.getQualifiedQty());
            if (plan <= 0) {
                plan = orZero(item.getReceivedQty());
            }
            if (plan <= 0) {
                continue;
            }
            PutawayTask task = new PutawayTask();
            task.setTaskNo("PW" + System.currentTimeMillis() + item.getId());
            task.setInboundId(inboundId);
            task.setItemId(item.getId());
            task.setSkuId(item.getSkuId());
            task.setWarehouseId(order.getWarehouseId());
            task.setLocationId(null);
            task.setPlanQty(plan);
            task.setActualQty(0);
            task.setStatus("PENDING");
            task.setOperator(operator);
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            task.setDeleted(0);
            putawayTaskMapper.insert(task);
        }
        order.setStatus(InboundStatus.PUTTING_AWAY);
        order.setUpdateTime(LocalDateTime.now());
        inboundOrderMapper.updateById(order);
    }

    private int orZero(Integer v) {
        return v == null ? 0 : v;
    }
}
