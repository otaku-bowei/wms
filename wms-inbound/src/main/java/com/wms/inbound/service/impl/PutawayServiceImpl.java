package com.wms.inbound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.api.inventory.client.StockFeignClient;
import com.wms.api.inventory.dto.StockChangeDTO;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.inbound.domain.InboundStatus;
import com.wms.inbound.domain.dto.PutawayConfirmVO;
import com.wms.inbound.domain.entity.InboundItem;
import com.wms.inbound.domain.entity.InboundOrder;
import com.wms.inbound.domain.entity.PutawayTask;
import com.wms.inbound.mapper.InboundItemMapper;
import com.wms.inbound.mapper.InboundOrderMapper;
import com.wms.inbound.mapper.PutawayTaskMapper;
import com.wms.inbound.service.PutawayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 上架任务服务实现：上架确认调用库存服务增加库存（跨服务最终一致）。
 */
@Service
@RequiredArgsConstructor
public class PutawayServiceImpl implements PutawayService {

    private final PutawayTaskMapper putawayTaskMapper;
    private final InboundItemMapper inboundItemMapper;
    private final InboundOrderMapper inboundOrderMapper;
    private final StockFeignClient stockFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(PutawayTask task) {
        task.setTaskNo("PW" + System.currentTimeMillis());
        task.setActualQty(orZero(task.getActualQty()));
        task.setStatus("PENDING");
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        task.setDeleted(0);
        putawayTaskMapper.insert(task);
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPutaway(PutawayConfirmVO vo) {
        PutawayTask task = putawayTaskMapper.selectById(vo.getTaskId());
        if (task == null || task.getDeleted() == 1) {
            throw new BizException(ErrorCode.INBOUND_NOT_FOUND);
        }
        if (!"PENDING".equals(task.getStatus())) {
            throw new BizException(ErrorCode.INBOUND_STATUS_INVALID);
        }
        int actualQty = orZero(vo.getActualQty());
        if (actualQty <= 0) {
            throw new BizException(ErrorCode.INBOUND_QTY_EXCEED);
        }
        int remaining = orZero(task.getPlanQty()) - orZero(task.getActualQty());
        if (actualQty > remaining) {
            throw new BizException(ErrorCode.INBOUND_QTY_EXCEED);
        }
        InboundItem item = inboundItemMapper.selectById(task.getItemId());
        if (item == null) {
            throw new BizException(ErrorCode.INBOUND_NOT_FOUND);
        }

        StockChangeDTO dto = new StockChangeDTO();
        dto.setSkuId(task.getSkuId());
        dto.setLocationId(task.getLocationId());
        dto.setBatchNo(item.getBatchNo());
        dto.setQuantity(actualQty);
        dto.setRelatedNo(task.getTaskNo());
        dto.setOperator(vo.getOperator());
        dto.setRemark(vo.getRemark());
        if (!stockFeignClient.increase(dto).isSuccess()) {
            throw new BizException(ErrorCode.SYSTEM_ERROR);
        }

        task.setActualQty(orZero(task.getActualQty()) + actualQty);
        task.setStatus("COMPLETED");
        task.setOperator(vo.getOperator());
        task.setOperateTime(LocalDateTime.now());
        task.setRemark(vo.getRemark());
        task.setUpdateTime(LocalDateTime.now());
        putawayTaskMapper.updateById(task);

        item.setPutawayQty(orZero(item.getPutawayQty()) + actualQty);
        item.setUpdateTime(LocalDateTime.now());
        inboundItemMapper.updateById(item);

        checkAndCompleteOrder(task.getInboundId());
    }

    private void checkAndCompleteOrder(Long inboundId) {
        List<InboundItem> items = inboundItemMapper.selectList(new LambdaQueryWrapper<InboundItem>()
                .eq(InboundItem::getInboundId, inboundId).eq(InboundItem::getDeleted, 0));
        boolean allDone = items.stream().allMatch(it -> orZero(it.getPutawayQty()) >= orZero(it.getPlanQty()) && orZero(it.getPlanQty()) > 0);
        if (allDone && !items.isEmpty()) {
            InboundOrder order = inboundOrderMapper.selectById(inboundId);
            if (order != null && InboundStatus.PUTTING_AWAY.equals(order.getStatus())) {
                order.setStatus(InboundStatus.COMPLETED);
                order.setUpdateTime(LocalDateTime.now());
                inboundOrderMapper.updateById(order);
            }
        }
    }

    private int orZero(Integer v) {
        return v == null ? 0 : v;
    }
}
