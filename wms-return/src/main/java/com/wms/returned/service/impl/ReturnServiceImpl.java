package com.wms.returned.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.api.inventory.client.StockFeignClient;
import com.wms.api.inventory.dto.StockChangeDTO;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.core.result.PageResult;
import com.wms.common.core.result.R;
import com.wms.common.mybatis.convert.PageConvert;
import com.wms.returned.domain.entity.ReturnItem;
import com.wms.returned.domain.entity.ReturnOrder;
import com.wms.returned.mapper.ReturnItemMapper;
import com.wms.returned.mapper.ReturnOrderMapper;
import com.wms.returned.service.ReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 退货服务实现：收货/质检分级/重新上架（A 级回写库存）。
 */
@Service
@RequiredArgsConstructor
public class ReturnServiceImpl extends ServiceImpl<ReturnOrderMapper, ReturnOrder> implements ReturnService {

    private final ReturnItemMapper returnItemMapper;
    private final StockFeignClient stockFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReturn(ReturnOrder order, List<ReturnItem> items) {
        order.setReturnNo("RN" + System.currentTimeMillis());
        order.setStatus("CREATED");
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDeleted(0);
        baseMapper.insert(order);
        if (items != null) {
            for (ReturnItem item : items) {
                item.setReturnId(order.getId());
                item.setReceivedQty(0);
                item.setReshelvedQty(0);
                item.setGrade(null);
                item.setCreateTime(LocalDateTime.now());
                item.setUpdateTime(LocalDateTime.now());
                item.setDeleted(0);
                returnItemMapper.insert(item);
            }
        }
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receive(Long id) {
        ReturnOrder order = requireOrder(id);
        if (!"CREATED".equals(order.getStatus())) {
            throw new BizException(ErrorCode.RETURN_STATUS_INVALID);
        }
        List<ReturnItem> items = returnItemMapper.selectList(new LambdaQueryWrapper<ReturnItem>()
                .eq(ReturnItem::getReturnId, id).eq(ReturnItem::getDeleted, 0));
        for (ReturnItem item : items) {
            item.setReceivedQty(orZero(item.getPlanQty()));
            item.setUpdateTime(LocalDateTime.now());
            returnItemMapper.updateById(item);
        }
        order.setStatus("RECEIVED");
        order.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inspect(Long id, List<ReturnItem> gradedItems) {
        ReturnOrder order = requireOrder(id);
        if (!"RECEIVED".equals(order.getStatus())) {
            throw new BizException(ErrorCode.RETURN_STATUS_INVALID);
        }
        for (ReturnItem g : gradedItems) {
            if (!"A".equals(g.getGrade()) && !"B".equals(g.getGrade()) && !"C".equals(g.getGrade())) {
                throw new BizException(ErrorCode.RETURN_GRADE_INVALID);
            }
            ReturnItem item = returnItemMapper.selectById(g.getId());
            if (item == null || Integer.valueOf(1).equals(item.getDeleted())) {
                throw new BizException(ErrorCode.RETURN_NOT_FOUND);
            }
            item.setGrade(g.getGrade());
            item.setUpdateTime(LocalDateTime.now());
            returnItemMapper.updateById(item);
        }
        order.setStatus("INSPECTED");
        order.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reshelve(Long id) {
        ReturnOrder order = requireOrder(id);
        if (!"INSPECTED".equals(order.getStatus())) {
            throw new BizException(ErrorCode.RETURN_STATUS_INVALID);
        }
        List<ReturnItem> items = returnItemMapper.selectList(new LambdaQueryWrapper<ReturnItem>()
                .eq(ReturnItem::getReturnId, id).eq(ReturnItem::getDeleted, 0));
        for (ReturnItem item : items) {
            if (!"A".equals(item.getGrade()) || item.getLocationId() == null) {
                item.setReshelvedQty(orZero(item.getReceivedQty()));
                item.setUpdateTime(LocalDateTime.now());
                returnItemMapper.updateById(item);
                continue;
            }
            StockChangeDTO dto = new StockChangeDTO();
            dto.setSkuId(item.getSkuId());
            dto.setLocationId(item.getLocationId());
            dto.setBatchNo(null);
            dto.setQuantity(orZero(item.getReceivedQty()));
            dto.setRelatedNo(order.getReturnNo());
            if (!stockFeignClient.increase(dto).isSuccess()) {
                throw new BizException(ErrorCode.SYSTEM_ERROR);
            }
            item.setReshelvedQty(orZero(item.getReceivedQty()));
            item.setUpdateTime(LocalDateTime.now());
            returnItemMapper.updateById(item);
        }
        order.setStatus("RESHELVED");
        order.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(order);
    }

    @Override
    public PageResult<ReturnOrder> page(long pageNum, long pageSize) {
        IPage<ReturnOrder> page = new Page<>(pageNum, pageSize);
        baseMapper.selectPage(page, new LambdaQueryWrapper<ReturnOrder>()
                .eq(ReturnOrder::getDeleted, 0).orderByDesc(ReturnOrder::getCreateTime));
        return PageConvert.toResult(page);
    }

    private ReturnOrder requireOrder(Long id) {
        ReturnOrder order = baseMapper.selectById(id);
        if (order == null || Integer.valueOf(1).equals(order.getDeleted())) {
            throw new BizException(ErrorCode.RETURN_NOT_FOUND);
        }
        return order;
    }

    private int orZero(Integer v) {
        return v == null ? 0 : v;
    }
}
