package com.wms.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.core.result.PageResult;
import com.wms.common.mybatis.convert.PageConvert;
import com.wms.inventory.domain.entity.CountItem;
import com.wms.inventory.domain.entity.CountOrder;
import com.wms.inventory.domain.entity.InvAdjust;
import com.wms.inventory.domain.entity.InvAdjustItem;
import com.wms.inventory.domain.entity.InvStock;
import com.wms.inventory.mapper.CountItemMapper;
import com.wms.inventory.mapper.CountOrderMapper;
import com.wms.inventory.mapper.InvStockMapper;
import com.wms.inventory.service.AdjustService;
import com.wms.inventory.service.CountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 盘点服务实现：录入实盘后生成差异并复用调整单回写库存。
 */
@Service
@RequiredArgsConstructor
public class CountServiceImpl extends ServiceImpl<CountOrderMapper, CountOrder> implements CountService {

    private final CountItemMapper countItemMapper;
    private final InvStockMapper stockMapper;
    private final AdjustService adjustService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCountOrder(CountOrder order, List<CountItem> items) {
        order.setCountNo("CN" + System.currentTimeMillis());
        order.setStatus("PENDING");
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDeleted(0);
        baseMapper.insert(order);
        if (items != null) {
            for (CountItem item : items) {
                InvStock stock = stockMapper.selectOne(new LambdaQueryWrapper<InvStock>()
                        .eq(InvStock::getSkuId, item.getSkuId())
                        .eq(InvStock::getLocationId, item.getLocationId())
                        .eq(item.getBatchNo() != null, InvStock::getBatchNo, item.getBatchNo())
                        .eq(InvStock::getDeleted, 0));
                item.setCountId(order.getId());
                item.setSystemQty(stock == null ? 0 : orZero(stock.getTotalQty()));
                item.setCountedQty(null);
                item.setDiffQty(0);
                item.setStatus("PENDING");
                item.setCreateTime(LocalDateTime.now());
                item.setUpdateTime(LocalDateTime.now());
                item.setDeleted(0);
                countItemMapper.insert(item);
            }
        }
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordCount(Long itemId, Integer countedQty) {
        CountItem item = countItemMapper.selectById(itemId);
        if (item == null || Integer.valueOf(1).equals(item.getDeleted())) {
            throw new BizException(ErrorCode.COUNT_NOT_FOUND);
        }
        if (!"PENDING".equals(item.getStatus())) {
            throw new BizException(ErrorCode.COUNT_STATUS_INVALID);
        }
        item.setCountedQty(countedQty);
        item.setDiffQty(orZero(countedQty) - orZero(item.getSystemQty()));
        item.setStatus("COUNTED");
        item.setUpdateTime(LocalDateTime.now());
        countItemMapper.updateById(item);

        CountOrder order = baseMapper.selectById(item.getCountId());
        if (order != null && "PENDING".equals(order.getStatus())) {
            order.setStatus("COUNTING");
            order.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(order);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateDiff(Long countId) {
        CountOrder order = baseMapper.selectById(countId);
        if (order == null || Integer.valueOf(1).equals(order.getDeleted())) {
            throw new BizException(ErrorCode.COUNT_NOT_FOUND);
        }
        if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new BizException(ErrorCode.COUNT_STATUS_INVALID);
        }
        List<CountItem> items = countItemMapper.selectList(new LambdaQueryWrapper<CountItem>()
                .eq(CountItem::getCountId, countId).eq(CountItem::getDeleted, 0)
                .eq(CountItem::getStatus, "COUNTED"));
        List<InvAdjustItem> adjustItems = new ArrayList<>();
        for (CountItem item : items) {
            if (orZero(item.getDiffQty()) == 0) {
                item.setStatus("ADJUSTED");
                countItemMapper.updateById(item);
                continue;
            }
            InvAdjustItem adj = new InvAdjustItem();
            adj.setSkuId(item.getSkuId());
            adj.setLocationId(item.getLocationId());
            adj.setBatchNo(item.getBatchNo());
            adj.setAfterQty(orZero(item.getCountedQty()));
            adjustItems.add(adj);

            item.setStatus("ADJUSTED");
            countItemMapper.updateById(item);
        }
        if (!adjustItems.isEmpty()) {
            InvAdjust adjust = new InvAdjust();
            adjust.setWarehouseId(order.getWarehouseId());
            adjust.setReason("CHECK");
            adjust.setRemark("盘点差异-" + order.getCountNo());
            adjust.setOperator(order.getOperator());
            Long adjustId = adjustService.createAdjust(adjust, adjustItems);
            adjustService.audit(adjustId, true, order.getOperator());
        }
        order.setStatus("COMPLETED");
        order.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(order);
    }

    @Override
    public PageResult<CountOrder> page(long pageNum, long pageSize) {
        IPage<CountOrder> page = new Page<>(pageNum, pageSize);
        baseMapper.selectPage(page, new LambdaQueryWrapper<CountOrder>()
                .eq(CountOrder::getDeleted, 0).orderByDesc(CountOrder::getCreateTime));
        return PageConvert.toResult(page);
    }

    private int orZero(Integer v) {
        return v == null ? 0 : v;
    }
}
