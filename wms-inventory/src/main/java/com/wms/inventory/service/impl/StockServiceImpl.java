package com.wms.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.api.inventory.dto.StockChangeDTO;
import com.wms.api.inventory.dto.StockVO;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.inventory.domain.entity.InvStock;
import com.wms.inventory.domain.entity.InvTransaction;
import com.wms.inventory.mapper.InvStockMapper;
import com.wms.inventory.mapper.InvTransactionMapper;
import com.wms.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 库存服务实现：基于乐观锁（inv_stock.version）更新，并落库存流水。
 */
@Service
@RequiredArgsConstructor
public class StockServiceImpl extends ServiceImpl<InvStockMapper, InvStock> implements StockService {

    private final InvTransactionMapper transactionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increase(StockChangeDTO dto) {
        InvStock stock = findOrCreate(dto.getSkuId(), dto.getLocationId(), dto.getBatchNo());
        int before = nullToZero(stock.getAvailableQty());
        int after = before + dto.getQuantity();
        stock.setAvailableQty(after);
        stock.setTotalQty(nullToZero(stock.getTotalQty()) + dto.getQuantity());
        baseMapper.updateById(stock);
        saveTransaction("INBOUND", dto.getQuantity(), before, after, dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrease(StockChangeDTO dto) {
        InvStock stock = requireStock(dto.getSkuId(), dto.getLocationId(), dto.getBatchNo());
        int before = nullToZero(stock.getAvailableQty());
        int after = before - dto.getQuantity();
        if (after < 0) {
            throw new BizException(ErrorCode.STOCK_NOT_ENOUGH);
        }
        stock.setAvailableQty(after);
        stock.setTotalQty(nullToZero(stock.getTotalQty()) - dto.getQuantity());
        baseMapper.updateById(stock);
        saveTransaction("OUTBOUND", -dto.getQuantity(), before, after, dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lock(StockChangeDTO dto) {
        InvStock stock = requireStock(dto.getSkuId(), dto.getLocationId(), dto.getBatchNo());
        int before = nullToZero(stock.getAvailableQty());
        int after = before - dto.getQuantity();
        if (after < 0) {
            throw new BizException(ErrorCode.STOCK_NOT_ENOUGH);
        }
        stock.setAvailableQty(after);
        stock.setLockedQty(nullToZero(stock.getLockedQty()) + dto.getQuantity());
        baseMapper.updateById(stock);
        saveTransaction("LOCK", -dto.getQuantity(), before, after, dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlock(StockChangeDTO dto) {
        InvStock stock = requireStock(dto.getSkuId(), dto.getLocationId(), dto.getBatchNo());
        int before = nullToZero(stock.getAvailableQty());
        int after = before + dto.getQuantity();
        stock.setAvailableQty(after);
        stock.setLockedQty(nullToZero(stock.getLockedQty()) - dto.getQuantity());
        baseMapper.updateById(stock);
        saveTransaction("UNLOCK", dto.getQuantity(), before, after, dto);
    }

    @Override
    public StockVO getLocationSummary(Long locationId) {
        List<InvStock> list = baseMapper.selectList(
                new LambdaQueryWrapper<InvStock>().eq(InvStock::getLocationId, locationId)
                        .eq(InvStock::getDeleted, 0));
        StockVO vo = new StockVO();
        vo.setLocationId(locationId);
        vo.setTotalQty(list.stream().mapToInt(s -> nullToZero(s.getTotalQty())).sum());
        vo.setAvailableQty(list.stream().mapToInt(s -> nullToZero(s.getAvailableQty())).sum());
        vo.setLockedQty(list.stream().mapToInt(s -> nullToZero(s.getLockedQty())).sum());
        vo.setFrozenQty(list.stream().mapToInt(s -> nullToZero(s.getFrozenQty())).sum());
        return vo;
    }

    private InvStock findOrCreate(Long skuId, Long locationId, String batchNo) {
        InvStock exist = baseMapper.selectOne(new LambdaQueryWrapper<InvStock>()
                .eq(InvStock::getSkuId, skuId)
                .eq(InvStock::getLocationId, locationId)
                .eq(batchNo != null, InvStock::getBatchNo, batchNo)
                .eq(InvStock::getDeleted, 0));
        if (exist != null) {
            return exist;
        }
        InvStock stock = new InvStock();
        stock.setSkuId(skuId);
        stock.setWarehouseId(null);
        stock.setLocationId(locationId);
        stock.setBatchNo(batchNo);
        stock.setTotalQty(0);
        stock.setAvailableQty(0);
        stock.setLockedQty(0);
        stock.setFrozenQty(0);
        stock.setStatus("NORMAL");
        stock.setDeleted(0);
        baseMapper.insert(stock);
        return stock;
    }

    private InvStock requireStock(Long skuId, Long locationId, String batchNo) {
        InvStock stock = baseMapper.selectOne(new LambdaQueryWrapper<InvStock>()
                .eq(InvStock::getSkuId, skuId)
                .eq(InvStock::getLocationId, locationId)
                .eq(batchNo != null, InvStock::getBatchNo, batchNo)
                .eq(InvStock::getDeleted, 0));
        if (stock == null) {
            throw new BizException(ErrorCode.STOCK_NOT_FOUND);
        }
        return stock;
    }

    private void saveTransaction(String type, int quantity, int before, int after, StockChangeDTO dto) {
        InvTransaction tx = new InvTransaction();
        tx.setTransactionNo("T" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        tx.setSkuId(dto.getSkuId());
        tx.setLocationId(dto.getLocationId());
        tx.setBatchNo(dto.getBatchNo());
        tx.setTransactionType(type);
        tx.setQuantity(quantity);
        tx.setBeforeQty(before);
        tx.setAfterQty(after);
        tx.setRelatedNo(dto.getRelatedNo());
        tx.setOperator(dto.getOperator());
        tx.setOperateTime(LocalDateTime.now());
        tx.setRemark(dto.getRemark());
        transactionMapper.insert(tx);
    }

    private int nullToZero(Integer v) {
        return v == null ? 0 : v;
    }
}
