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
import com.wms.outbound.domain.WaveStatus;
import com.wms.outbound.domain.dto.WaveGenerateVO;
import com.wms.outbound.domain.entity.OutboundItem;
import com.wms.outbound.domain.entity.OutboundOrder;
import com.wms.outbound.domain.entity.WaveItem;
import com.wms.outbound.domain.entity.WaveOrder;
import com.wms.outbound.mapper.OutboundItemMapper;
import com.wms.outbound.mapper.OutboundOrderMapper;
import com.wms.outbound.mapper.WaveItemMapper;
import com.wms.outbound.mapper.WaveOrderMapper;
import com.wms.outbound.service.WaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 波次服务实现：生成波次时纳入出库单并锁定库存。
 */
@Service
@RequiredArgsConstructor
public class WaveServiceImpl extends ServiceImpl<WaveOrderMapper, WaveOrder> implements WaveService {

    private final OutboundOrderMapper outboundOrderMapper;
    private final OutboundItemMapper outboundItemMapper;
    private final WaveItemMapper waveItemMapper;
    private final StockFeignClient stockFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWave(WaveOrder wave) {
        wave.setWaveNo("WV" + System.currentTimeMillis());
        wave.setStatus(WaveStatus.PENDING);
        wave.setMaxOrders(wave.getMaxOrders() == null ? 50 : wave.getMaxOrders());
        wave.setCreateTime(LocalDateTime.now());
        wave.setUpdateTime(LocalDateTime.now());
        wave.setDeleted(0);
        baseMapper.insert(wave);
        return wave.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generate(WaveGenerateVO vo) {
        WaveOrder wave = baseMapper.selectById(vo.getWaveId());
        if (wave == null) {
            throw new BizException(ErrorCode.WAVE_NOT_FOUND);
        }
        if (!WaveStatus.PENDING.equals(wave.getStatus())) {
            throw new BizException(ErrorCode.WAVE_STATUS_INVALID);
        }
        wave.setStatus(WaveStatus.GENERATING);
        wave.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(wave);
        for (Long orderId : vo.getOrderIds()) {
            OutboundOrder order = outboundOrderMapper.selectById(orderId);
            if (order == null || !OutboundStatus.DRAFT.equals(order.getStatus())) {
                throw new BizException(ErrorCode.OUTBOUND_STATUS_INVALID);
            }
            List<OutboundItem> items = outboundItemMapper.selectList(new LambdaQueryWrapper<OutboundItem>()
                    .eq(OutboundItem::getOutboundId, orderId).eq(OutboundItem::getDeleted, 0));
            for (OutboundItem item : items) {
                StockChangeDTO dto = new StockChangeDTO();
                dto.setSkuId(item.getSkuId());
                dto.setLocationId(item.getLocationId());
                dto.setBatchNo(item.getBatchNo());
                dto.setQuantity(orZero(item.getPlanQty()));
                dto.setRelatedNo(order.getOutboundNo());
                if (!stockFeignClient.lock(dto).isSuccess()) {
                    throw new BizException(ErrorCode.OUTBOUND_STOCK_LOCK_FAIL);
                }
            }
            order.setStatus(OutboundStatus.ALLOCATED);
            order.setUpdateTime(LocalDateTime.now());
            outboundOrderMapper.updateById(order);
            WaveItem waveItem = new WaveItem();
            waveItem.setWaveId(wave.getId());
            waveItem.setOutboundId(orderId);
            waveItem.setStatus("PENDING");
            waveItem.setCreateTime(LocalDateTime.now());
            waveItem.setUpdateTime(LocalDateTime.now());
            waveItem.setDeleted(0);
            waveItemMapper.insert(waveItem);
        }
        wave.setStatus(WaveStatus.GENERATED);
        wave.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(wave);
    }

    @Override
    public PageResult<WaveOrder> page(long pageNum, long pageSize) {
        IPage<WaveOrder> page = new Page<>(pageNum, pageSize);
        baseMapper.selectPage(page, new LambdaQueryWrapper<WaveOrder>()
                .eq(WaveOrder::getDeleted, 0)
                .orderByDesc(WaveOrder::getCreateTime));
        return PageConvert.toResult(page);
    }

    private int orZero(Integer v) {
        return v == null ? 0 : v;
    }
}
