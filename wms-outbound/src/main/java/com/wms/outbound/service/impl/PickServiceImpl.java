package com.wms.outbound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.core.result.PageResult;
import com.wms.common.mybatis.convert.PageConvert;
import com.wms.outbound.domain.OutboundStatus;
import com.wms.outbound.domain.dto.PickConfirmVO;
import com.wms.outbound.domain.entity.OutboundItem;
import com.wms.outbound.domain.entity.OutboundOrder;
import com.wms.outbound.domain.entity.PickTask;
import com.wms.outbound.domain.entity.WaveItem;
import com.wms.outbound.mapper.OutboundItemMapper;
import com.wms.outbound.mapper.OutboundOrderMapper;
import com.wms.outbound.mapper.WaveItemMapper;
import com.wms.outbound.mapper.PickTaskMapper;
import com.wms.outbound.service.PickService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 拣货服务实现：生成拣货任务 + 拣货确认回写。
 */
@Service
@RequiredArgsConstructor
public class PickServiceImpl extends ServiceImpl<PickTaskMapper, PickTask> implements PickService {

    private final OutboundItemMapper outboundItemMapper;
    private final OutboundOrderMapper outboundOrderMapper;
    private final WaveItemMapper waveItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTasks(Long waveId) {
        List<WaveItem> waveItems = waveItemMapper.selectList(new LambdaQueryWrapper<WaveItem>()
                .eq(WaveItem::getWaveId, waveId).eq(WaveItem::getDeleted, 0)
                .eq(WaveItem::getStatus, "PENDING"));
        for (WaveItem waveItem : waveItems) {
            OutboundOrder order = outboundOrderMapper.selectById(waveItem.getOutboundId());
            if (order == null) {
                continue;
            }
            List<OutboundItem> items = outboundItemMapper.selectList(new LambdaQueryWrapper<OutboundItem>()
                    .eq(OutboundItem::getOutboundId, waveItem.getOutboundId()).eq(OutboundItem::getDeleted, 0));
            for (OutboundItem item : items) {
                PickTask task = new PickTask();
                task.setTaskNo("PK" + System.currentTimeMillis() + item.getId());
                task.setWaveId(waveId);
                task.setOutboundId(item.getOutboundId());
                task.setItemId(item.getId());
                task.setSkuId(item.getSkuId());
                task.setWarehouseId(order.getWarehouseId());
                task.setLocationId(item.getLocationId());
                task.setPlanQty(orZero(item.getPlanQty()));
                task.setActualQty(0);
                task.setStatus("PENDING");
                task.setCreateTime(LocalDateTime.now());
                task.setUpdateTime(LocalDateTime.now());
                task.setDeleted(0);
                baseMapper.insert(task);
            }
            if (!OutboundStatus.PICKING.equals(order.getStatus())) {
                order.setStatus(OutboundStatus.PICKING);
                order.setUpdateTime(LocalDateTime.now());
                outboundOrderMapper.updateById(order);
            }
            waveItem.setStatus("PICKING");
            waveItem.setUpdateTime(LocalDateTime.now());
            waveItemMapper.updateById(waveItem);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPick(PickConfirmVO vo) {
        PickTask task = baseMapper.selectById(vo.getTaskId());
        if (task == null || Integer.valueOf(1).equals(task.getDeleted())) {
            throw new BizException(ErrorCode.PICK_TASK_NOT_FOUND);
        }
        if (!"PENDING".equals(task.getStatus())) {
            throw new BizException(ErrorCode.OUTBOUND_STATUS_INVALID);
        }
        int actualQty = orZero(vo.getActualQty());
        if (actualQty <= 0) {
            throw new BizException(ErrorCode.OUTBOUND_QTY_EXCEED);
        }
        int remaining = orZero(task.getPlanQty()) - orZero(task.getActualQty());
        if (actualQty > remaining) {
            throw new BizException(ErrorCode.OUTBOUND_QTY_EXCEED);
        }
        OutboundItem item = outboundItemMapper.selectById(task.getItemId());
        if (item == null) {
            throw new BizException(ErrorCode.OUTBOUND_NOT_FOUND);
        }
        task.setActualQty(orZero(task.getActualQty()) + actualQty);
        task.setStatus("PICKED");
        if (vo.getLocationId() != null) {
            task.setLocationId(vo.getLocationId());
        }
        task.setPicker(vo.getPicker());
        task.setPickTime(LocalDateTime.now());
        task.setRemark(vo.getRemark());
        task.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(task);

        item.setPickedQty(orZero(item.getPickedQty()) + actualQty);
        item.setLocationId(task.getLocationId());
        item.setUpdateTime(LocalDateTime.now());
        outboundItemMapper.updateById(item);

        checkAndCompleteOrder(task.getOutboundId());
    }

    @Override
    public PageResult<PickTask> page(long pageNum, long pageSize) {
        IPage<PickTask> page = new Page<>(pageNum, pageSize);
        baseMapper.selectPage(page, new LambdaQueryWrapper<PickTask>()
                .eq(PickTask::getDeleted, 0)
                .orderByDesc(PickTask::getCreateTime));
        return PageConvert.toResult(page);
    }

    private void checkAndCompleteOrder(Long outboundId) {
        List<PickTask> tasks = baseMapper.selectList(new LambdaQueryWrapper<PickTask>()
                .eq(PickTask::getOutboundId, outboundId).eq(PickTask::getDeleted, 0));
        boolean allPicked = !tasks.isEmpty() && tasks.stream().allMatch(t -> "PICKED".equals(t.getStatus()));
        if (allPicked) {
            OutboundOrder order = outboundOrderMapper.selectById(outboundId);
            if (order != null && OutboundStatus.PICKING.equals(order.getStatus())) {
                order.setStatus(OutboundStatus.PICKED);
                order.setUpdateTime(LocalDateTime.now());
                outboundOrderMapper.updateById(order);
            }
            List<WaveItem> waveItems = waveItemMapper.selectList(new LambdaQueryWrapper<WaveItem>()
                    .eq(WaveItem::getOutboundId, outboundId).eq(WaveItem::getDeleted, 0));
            for (WaveItem waveItem : waveItems) {
                waveItem.setStatus("PICKED");
                waveItem.setUpdateTime(LocalDateTime.now());
                waveItemMapper.updateById(waveItem);
            }
        }
    }

    private int orZero(Integer v) {
        return v == null ? 0 : v;
    }
}
