package com.wms.outbound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.api.inventory.client.StockFeignClient;
import com.wms.api.inventory.dto.StockChangeDTO;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.outbound.domain.OutboundStatus;
import com.wms.outbound.domain.dto.PackVO;
import com.wms.outbound.domain.dto.ShipVO;
import com.wms.outbound.domain.entity.OutboundItem;
import com.wms.outbound.domain.entity.OutboundOrder;
import com.wms.outbound.domain.entity.OutboundPackage;
import com.wms.outbound.domain.entity.ShippingRecord;
import com.wms.outbound.mapper.OutboundItemMapper;
import com.wms.outbound.mapper.OutboundOrderMapper;
import com.wms.outbound.mapper.OutboundPackageMapper;
import com.wms.outbound.mapper.ShippingRecordMapper;
import com.wms.outbound.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 复核打包与装车服务实现：打包进入 PACKING；发货扣减库存并置 SHIPPED。
 */
@Service
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {

    private final OutboundOrderMapper outboundOrderMapper;
    private final OutboundItemMapper outboundItemMapper;
    private final OutboundPackageMapper outboundPackageMapper;
    private final ShippingRecordMapper shippingRecordMapper;
    private final StockFeignClient stockFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long pack(PackVO vo) {
        OutboundOrder order = requireOrder(vo.getOutboundId());
        if (!OutboundStatus.PICKED.equals(order.getStatus())) {
            throw new BizException(ErrorCode.OUTBOUND_STATUS_INVALID);
        }
        OutboundPackage pkg = new OutboundPackage();
        pkg.setPackageNo("PKG" + System.currentTimeMillis());
        pkg.setOutboundId(vo.getOutboundId());
        pkg.setCarrierId(vo.getCarrierId());
        pkg.setTrackingNo(vo.getTrackingNo());
        pkg.setWeight(vo.getWeight());
        pkg.setStatus("PACKED");
        pkg.setPacker(vo.getPacker());
        pkg.setPackTime(LocalDateTime.now());
        pkg.setRemark(vo.getRemark());
        pkg.setCreateTime(LocalDateTime.now());
        pkg.setUpdateTime(LocalDateTime.now());
        pkg.setDeleted(0);
        outboundPackageMapper.insert(pkg);

        order.setStatus(OutboundStatus.PACKING);
        order.setUpdateTime(LocalDateTime.now());
        outboundOrderMapper.updateById(order);
        return pkg.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ship(ShipVO vo) {
        OutboundOrder order = requireOrder(vo.getOutboundId());
        if (!OutboundStatus.PACKING.equals(order.getStatus())) {
            throw new BizException(ErrorCode.OUTBOUND_STATUS_INVALID);
        }
        List<OutboundItem> items = outboundItemMapper.selectList(new LambdaQueryWrapper<OutboundItem>()
                .eq(OutboundItem::getOutboundId, vo.getOutboundId()).eq(OutboundItem::getDeleted, 0));
        for (OutboundItem item : items) {
            int shipQty = orZero(item.getPickedQty()) > 0 ? orZero(item.getPickedQty()) : orZero(item.getPlanQty());
            StockChangeDTO unlockDto = new StockChangeDTO();
            unlockDto.setSkuId(item.getSkuId());
            unlockDto.setLocationId(item.getLocationId());
            unlockDto.setBatchNo(item.getBatchNo());
            unlockDto.setQuantity(orZero(item.getPlanQty()));
            unlockDto.setRelatedNo(order.getOutboundNo());
            if (!stockFeignClient.unlock(unlockDto).isSuccess()) {
                throw new BizException(ErrorCode.OUTBOUND_STOCK_DECREASE_FAIL);
            }
            StockChangeDTO decDto = new StockChangeDTO();
            decDto.setSkuId(item.getSkuId());
            decDto.setLocationId(item.getLocationId());
            decDto.setBatchNo(item.getBatchNo());
            decDto.setQuantity(shipQty);
            decDto.setRelatedNo(order.getOutboundNo());
            if (!stockFeignClient.decrease(decDto).isSuccess()) {
                throw new BizException(ErrorCode.OUTBOUND_STOCK_DECREASE_FAIL);
            }
            item.setShippedQty(shipQty);
            item.setUpdateTime(LocalDateTime.now());
            outboundItemMapper.updateById(item);
        }

        order.setStatus(OutboundStatus.SHIPPED);
        order.setShipTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        outboundOrderMapper.updateById(order);

        OutboundPackage pkg = outboundPackageMapper.selectOne(new LambdaQueryWrapper<OutboundPackage>()
                .eq(OutboundPackage::getOutboundId, vo.getOutboundId())
                .eq(OutboundPackage::getDeleted, 0).orderByDesc(OutboundPackage::getCreateTime).last("LIMIT 1"));
        if (pkg != null) {
            pkg.setStatus("SHIPPED");
            pkg.setTrackingNo(vo.getTrackingNo() == null ? pkg.getTrackingNo() : vo.getTrackingNo());
            pkg.setUpdateTime(LocalDateTime.now());
            outboundPackageMapper.updateById(pkg);
        }

        ShippingRecord record = new ShippingRecord();
        record.setOutboundId(vo.getOutboundId());
        record.setPackageId(pkg == null ? null : pkg.getId());
        record.setCarrierId(vo.getCarrierId());
        record.setTrackingNo(vo.getTrackingNo());
        record.setVehicleNo(vo.getVehicleNo());
        record.setStatus("LOADED");
        record.setLoader(vo.getLoader());
        record.setLoadTime(LocalDateTime.now());
        record.setRemark(vo.getRemark());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setDeleted(0);
        shippingRecordMapper.insert(record);
    }

    private OutboundOrder requireOrder(Long id) {
        OutboundOrder order = outboundOrderMapper.selectById(id);
        if (order == null || Integer.valueOf(1).equals(order.getDeleted())) {
            throw new BizException(ErrorCode.OUTBOUND_NOT_FOUND);
        }
        return order;
    }

    private int orZero(Integer v) {
        return v == null ? 0 : v;
    }
}
