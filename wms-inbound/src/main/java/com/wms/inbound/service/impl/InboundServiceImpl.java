package com.wms.inbound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.core.result.PageResult;
import com.wms.common.mybatis.convert.PageConvert;
import com.wms.inbound.domain.InboundStatus;
import com.wms.inbound.domain.entity.InboundItem;
import com.wms.inbound.domain.entity.InboundOrder;
import com.wms.inbound.mapper.InboundItemMapper;
import com.wms.inbound.mapper.InboundOrderMapper;
import com.wms.inbound.service.InboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库单服务实现：状态机驱动预约/到货/质检流转。
 */
@Service
@RequiredArgsConstructor
public class InboundServiceImpl extends ServiceImpl<InboundOrderMapper, InboundOrder> implements InboundService {

    private final InboundItemMapper inboundItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(InboundOrder order, List<InboundItem> items) {
        order.setInboundNo("IN" + System.currentTimeMillis());
        order.setStatus(InboundStatus.DRAFT);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDeleted(0);
        baseMapper.insert(order);
        if (items != null) {
            for (InboundItem item : items) {
                item.setInboundId(order.getId());
                item.setReceivedQty(orZero(item.getReceivedQty()));
                item.setQualifiedQty(orZero(item.getQualifiedQty()));
                item.setDefectQty(orZero(item.getDefectQty()));
                item.setPutawayQty(orZero(item.getPutawayQty()));
                item.setCreateTime(LocalDateTime.now());
                item.setUpdateTime(LocalDateTime.now());
                item.setDeleted(0);
                inboundItemMapper.insert(item);
            }
        }
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long id) {
        changeStatus(id, InboundStatus.DRAFT, InboundStatus.CONFIRMED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void arrive(Long id) {
        InboundOrder order = requireOrder(id);
        changeStatus(order, InboundStatus.CONFIRMED, InboundStatus.ARRIVED);
        order.setArriveTime(LocalDateTime.now());
        baseMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitQc(Long id) {
        changeStatus(id, InboundStatus.ARRIVED, InboundStatus.PENDING_QC);
    }

    @Override
    public PageResult<InboundOrder> page(long pageNum, long pageSize) {
        IPage<InboundOrder> page = new Page<>(pageNum, pageSize);
        baseMapper.selectPage(page, new LambdaQueryWrapper<InboundOrder>()
                .eq(InboundOrder::getDeleted, 0)
                .orderByDesc(InboundOrder::getCreateTime));
        return PageConvert.toResult(page);
    }

    private void changeStatus(Long id, String expected, String target) {
        changeStatus(requireOrder(id), expected, target);
    }

    private void changeStatus(InboundOrder order, String expected, String target) {
        if (InboundStatus.isTerminal(order.getStatus())) {
            throw new BizException(ErrorCode.INBOUND_STATUS_INVALID);
        }
        if (!expected.equals(order.getStatus())) {
            throw new BizException(ErrorCode.INBOUND_STATUS_INVALID);
        }
        if (!InboundStatus.canTransition(order.getStatus(), target)) {
            throw new BizException(ErrorCode.INBOUND_STATUS_INVALID);
        }
        order.setStatus(target);
        order.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(order);
    }

    private InboundOrder requireOrder(Long id) {
        InboundOrder order = baseMapper.selectById(id);
        if (order == null || order.getDeleted() == 1) {
            throw new BizException(ErrorCode.INBOUND_NOT_FOUND);
        }
        return order;
    }

    private int orZero(Integer v) {
        return v == null ? 0 : v;
    }
}
