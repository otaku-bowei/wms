package com.wms.outbound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.api.inventory.client.StockFeignClient;
import com.wms.api.inventory.dto.StockChangeDTO;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.core.result.PageResult;
import com.wms.common.mybatis.convert.PageConvert;
import com.wms.outbound.domain.OutboundStatus;
import com.wms.outbound.domain.entity.OutboundItem;
import com.wms.outbound.domain.entity.OutboundOrder;
import com.wms.outbound.mapper.OutboundItemMapper;
import com.wms.outbound.mapper.OutboundOrderMapper;
import com.wms.outbound.service.OutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库单服务实现：状态机 + 库存锁定/释放。
 */
@Service
@RequiredArgsConstructor
public class OutboundServiceImpl extends ServiceImpl<OutboundOrderMapper, OutboundOrder> implements OutboundService {

    private final OutboundItemMapper outboundItemMapper;
    private final StockFeignClient stockFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OutboundOrder order, List<OutboundItem> items) {
        order.setOutboundNo("OUT" + System.currentTimeMillis());
        order.setStatus(OutboundStatus.DRAFT);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDeleted(0);
        baseMapper.insert(order);
        if (items != null) {
            for (OutboundItem item : items) {
                item.setOutboundId(order.getId());
                item.setPickedQty(orZero(item.getPickedQty()));
                item.setPackedQty(orZero(item.getPackedQty()));
                item.setShippedQty(orZero(item.getShippedQty()));
                item.setCreateTime(LocalDateTime.now());
                item.setUpdateTime(LocalDateTime.now());
                item.setDeleted(0);
                outboundItemMapper.insert(item);
            }
        }
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void allocate(Long id) {
        OutboundOrder order = requireOrder(id);
        if (!OutboundStatus.DRAFT.equals(order.getStatus())) {
            throw new BizException(ErrorCode.OUTBOUND_STATUS_INVALID);
        }
        List<OutboundItem> items = outboundItemMapper.selectList(new LambdaQueryWrapper<OutboundItem>()
                .eq(OutboundItem::getOutboundId, id).eq(OutboundItem::getDeleted, 0));
        for (OutboundItem item : items) {
            StockChangeDTO dto = buildDto(order.getOutboundNo(), item, orZero(item.getPlanQty()));
            if (!stockFeignClient.lock(dto).isSuccess()) {
                throw new BizException(ErrorCode.OUTBOUND_STOCK_LOCK_FAIL);
            }
        }
        order.setStatus(OutboundStatus.ALLOCATED);
        order.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        OutboundOrder order = requireOrder(id);
        if (OutboundStatus.isTerminal(order.getStatus())) {
            throw new BizException(ErrorCode.OUTBOUND_STATUS_INVALID);
        }
        if (OutboundStatus.ALLOCATED.equals(order.getStatus())) {
            List<OutboundItem> items = outboundItemMapper.selectList(new LambdaQueryWrapper<OutboundItem>()
                    .eq(OutboundItem::getOutboundId, id).eq(OutboundItem::getDeleted, 0));
            for (OutboundItem item : items) {
                StockChangeDTO dto = buildDto(order.getOutboundNo(), item, orZero(item.getPlanQty()));
                stockFeignClient.unlock(dto);
            }
        }
        order.setStatus(OutboundStatus.CANCELLED);
        order.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(order);
    }

    @Override
    public PageResult<OutboundOrder> page(long pageNum, long pageSize) {
        IPage<OutboundOrder> page = new Page<>(pageNum, pageSize);
        baseMapper.selectPage(page, new LambdaQueryWrapper<OutboundOrder>()
                .eq(OutboundOrder::getDeleted, 0)
                .orderByDesc(OutboundOrder::getCreateTime));
        return PageConvert.toResult(page);
    }

    private StockChangeDTO buildDto(String relatedNo, OutboundItem item, int qty) {
        StockChangeDTO dto = new StockChangeDTO();
        dto.setSkuId(item.getSkuId());
        dto.setLocationId(item.getLocationId());
        dto.setBatchNo(item.getBatchNo());
        dto.setQuantity(qty);
        dto.setRelatedNo(relatedNo);
        return dto;
    }

    OutboundOrder requireOrder(Long id) {
        OutboundOrder order = baseMapper.selectById(id);
        if (order == null || Integer.valueOf(1).equals(order.getDeleted())) {
            throw new BizException(ErrorCode.OUTBOUND_NOT_FOUND);
        }
        return order;
    }

    int orZero(Integer v) {
        return v == null ? 0 : v;
    }
}
