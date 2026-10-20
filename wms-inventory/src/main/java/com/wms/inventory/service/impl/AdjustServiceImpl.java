package com.wms.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.api.inventory.dto.StockChangeDTO;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.core.result.PageResult;
import com.wms.common.mybatis.convert.PageConvert;
import com.wms.inventory.domain.entity.InvAdjust;
import com.wms.inventory.domain.entity.InvAdjustItem;
import com.wms.inventory.domain.entity.InvStock;
import com.wms.inventory.mapper.InvAdjustItemMapper;
import com.wms.inventory.mapper.InvAdjustMapper;
import com.wms.inventory.mapper.InvStockMapper;
import com.wms.inventory.service.AdjustService;
import com.wms.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存调整单服务实现：审批通过后调用库存增减。
 */
@Service
@RequiredArgsConstructor
public class AdjustServiceImpl extends ServiceImpl<InvAdjustMapper, InvAdjust> implements AdjustService {

    private final InvAdjustItemMapper adjustItemMapper;
    private final InvStockMapper stockMapper;
    private final StockService stockService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAdjust(InvAdjust adjust, List<InvAdjustItem> items) {
        adjust.setAdjustNo("ADJ" + System.currentTimeMillis());
        adjust.setStatus("DRAFT");
        adjust.setCreateTime(LocalDateTime.now());
        adjust.setUpdateTime(LocalDateTime.now());
        adjust.setDeleted(0);
        baseMapper.insert(adjust);
        for (InvAdjustItem item : items) {
            item.setAdjustId(adjust.getId());
            InvStock stock = stockMapper.selectOne(new LambdaQueryWrapper<InvStock>()
                    .eq(InvStock::getSkuId, item.getSkuId())
                    .eq(InvStock::getLocationId, item.getLocationId())
                    .eq(item.getBatchNo() != null, InvStock::getBatchNo, item.getBatchNo())
                    .eq(InvStock::getDeleted, 0));
            int before = stock == null ? 0 : (stock.getAvailableQty() == null ? 0 : stock.getAvailableQty());
            item.setBeforeQty(before);
            item.setDiffQty((item.getAfterQty() == null ? 0 : item.getAfterQty()) - before);
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            item.setDeleted(0);
            adjustItemMapper.insert(item);
        }
        return adjust.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, boolean approved, String auditor) {
        InvAdjust adjust = baseMapper.selectById(id);
        if (adjust == null) {
            throw new BizException(ErrorCode.ADJUST_NOT_FOUND);
        }
        if (!"PENDING".equals(adjust.getStatus()) && !"DRAFT".equals(adjust.getStatus())) {
            throw new BizException(ErrorCode.ADJUST_STATUS_INVALID);
        }
        adjust.setAuditor(auditor);
        adjust.setAuditTime(LocalDateTime.now());
        adjust.setUpdateTime(LocalDateTime.now());
        if (!approved) {
            adjust.setStatus("REJECTED");
            baseMapper.updateById(adjust);
            return;
        }
        adjust.setStatus("APPROVED");
        baseMapper.updateById(adjust);
        List<InvAdjustItem> items = adjustItemMapper.selectList(
                new LambdaQueryWrapper<InvAdjustItem>().eq(InvAdjustItem::getAdjustId, id).eq(InvAdjustItem::getDeleted, 0));
        for (InvAdjustItem item : items) {
            StockChangeDTO dto = new StockChangeDTO();
            dto.setSkuId(item.getSkuId());
            dto.setLocationId(item.getLocationId());
            dto.setBatchNo(item.getBatchNo());
            dto.setRelatedNo(adjust.getAdjustNo());
            dto.setOperator(auditor);
            dto.setRemark(adjust.getRemark());
            int diff = item.getDiffQty() == null ? 0 : item.getDiffQty();
            if (diff > 0) {
                dto.setQuantity(diff);
                stockService.increase(dto);
            } else if (diff < 0) {
                dto.setQuantity(-diff);
                stockService.decrease(dto);
            }
        }
    }

    @Override
    public PageResult<InvAdjust> page(long pageNum, long pageSize) {
        IPage<InvAdjust> page = new Page<>(pageNum, pageSize);
        baseMapper.selectPage(page, new LambdaQueryWrapper<InvAdjust>().eq(InvAdjust::getDeleted, 0)
                .orderByDesc(InvAdjust::getCreateTime));
        return PageConvert.toResult(page);
    }
}
