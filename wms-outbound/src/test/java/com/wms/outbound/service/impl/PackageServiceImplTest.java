package com.wms.outbound.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.wms.api.inventory.client.StockFeignClient;
import com.wms.api.inventory.dto.StockChangeDTO;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.core.result.R;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 复核打包与装车服务单元测试：覆盖打包进入 PACKING、发货扣减库存并置 SHIPPED。
 */
@ExtendWith(MockitoExtension.class)
class PackageServiceImplTest {

    @Mock
    private OutboundOrderMapper outboundOrderMapper;

    @Mock
    private OutboundItemMapper outboundItemMapper;

    @Mock
    private OutboundPackageMapper outboundPackageMapper;

    @Mock
    private ShippingRecordMapper shippingRecordMapper;

    @Mock
    private StockFeignClient stockFeignClient;

    @InjectMocks
    private PackageServiceImpl packageService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("打包：PICKED 订单生成包裹并进入 PACKING")
    void pack_shouldCreatePackageAndEnterPacking() {
        when(outboundOrderMapper.selectById(2L)).thenReturn(buildOrder(2L, OutboundStatus.PICKED));

        Long pkgId = packageService.pack(buildPackVO(2L, 5L, "TN1", new BigDecimal("1.2"), "packer1", null));

        assertThat(pkgId).isNull(); // 单元测试下 insert 被 mock，ASSIGN_ID 不生效
        ArgumentCaptor<OutboundPackage> pkgCaptor = ArgumentCaptor.forClass(OutboundPackage.class);
        verify(outboundPackageMapper).insert(pkgCaptor.capture());
        assertThat(pkgCaptor.getValue().getStatus()).isEqualTo("PACKED");
        assertThat(pkgCaptor.getValue().getTrackingNo()).isEqualTo("TN1");

        ArgumentCaptor<OutboundOrder> orderCaptor = ArgumentCaptor.forClass(OutboundOrder.class);
        verify(outboundOrderMapper).updateById(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OutboundStatus.PACKING);
    }

    @Test
    @DisplayName("打包：非 PICKED 状态抛出 24003")
    void pack_shouldThrow_whenStatusInvalid() {
        when(outboundOrderMapper.selectById(2L)).thenReturn(buildOrder(2L, OutboundStatus.ALLOCATED));

        BizException ex = assertThrows(BizException.class,
                () -> packageService.pack(buildPackVO(2L, 5L, "TN1", null, "p", null)));
        assertThat(ex.getCode()).isEqualTo(24003);
    }

    @Test
    @DisplayName("发货：PACKING 订单解锁并扣减库存，置 SHIPPED 并生成装车记录")
    void ship_shouldDecreaseStockAndShip() {
        OutboundOrder order = buildOrder(2L, OutboundStatus.PACKING);
        OutboundItem item = buildItem(3L, 5, 5, 20L, "B1");
        OutboundPackage pkg = new OutboundPackage();
        pkg.setId(9L);
        pkg.setStatus("PACKED");
        when(outboundOrderMapper.selectById(2L)).thenReturn(order);
        when(outboundItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(item));
        when(outboundPackageMapper.selectOne(any(Wrapper.class))).thenReturn(pkg);
        when(stockFeignClient.unlock(any(StockChangeDTO.class))).thenReturn(R.ok());
        when(stockFeignClient.decrease(any(StockChangeDTO.class))).thenReturn(R.ok());

        packageService.ship(buildShipVO(2L, 5L, "TN1", "V001", "loader1", null));

        verify(stockFeignClient).unlock(any(StockChangeDTO.class));
        verify(stockFeignClient).decrease(any(StockChangeDTO.class));
        ArgumentCaptor<OutboundOrder> orderCaptor = ArgumentCaptor.forClass(OutboundOrder.class);
        verify(outboundOrderMapper).updateById(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OutboundStatus.SHIPPED);

        ArgumentCaptor<ShippingRecord> shipCaptor = ArgumentCaptor.forClass(ShippingRecord.class);
        verify(shippingRecordMapper).insert(shipCaptor.capture());
        assertThat(shipCaptor.getValue().getStatus()).isEqualTo("LOADED");
        assertThat(shipCaptor.getValue().getTrackingNo()).isEqualTo("TN1");
    }

    @Test
    @DisplayName("发货：非 PACKING 状态抛出 24003")
    void ship_shouldThrow_whenStatusInvalid() {
        when(outboundOrderMapper.selectById(2L)).thenReturn(buildOrder(2L, OutboundStatus.PICKED));

        BizException ex = assertThrows(BizException.class,
                () -> packageService.ship(buildShipVO(2L, 5L, "TN1", "V001", "l", null)));
        assertThat(ex.getCode()).isEqualTo(24003);
        verify(stockFeignClient, never()).decrease(any(StockChangeDTO.class));
    }

    private OutboundOrder buildOrder(Long id, String status) {
        OutboundOrder order = new OutboundOrder();
        order.setId(id);
        order.setOutboundNo("OUT" + id);
        order.setWarehouseId(1L);
        order.setStatus(status);
        return order;
    }

    private OutboundItem buildItem(Long id, int planQty, int pickedQty, Long locationId, String batchNo) {
        OutboundItem item = new OutboundItem();
        item.setId(id);
        item.setOutboundId(2L);
        item.setSkuId(10L);
        item.setLocationId(locationId);
        item.setBatchNo(batchNo);
        item.setPlanQty(planQty);
        item.setPickedQty(pickedQty);
        return item;
    }

    private PackVO buildPackVO(Long outboundId, Long carrierId, String trackingNo, BigDecimal weight,
                               String packer, String remark) {
        PackVO vo = new PackVO();
        vo.setOutboundId(outboundId);
        vo.setCarrierId(carrierId);
        vo.setTrackingNo(trackingNo);
        vo.setWeight(weight);
        vo.setPacker(packer);
        vo.setRemark(remark);
        return vo;
    }

    private ShipVO buildShipVO(Long outboundId, Long carrierId, String trackingNo, String vehicleNo,
                               String loader, String remark) {
        ShipVO vo = new ShipVO();
        vo.setOutboundId(outboundId);
        vo.setCarrierId(carrierId);
        vo.setTrackingNo(trackingNo);
        vo.setVehicleNo(vehicleNo);
        vo.setLoader(loader);
        vo.setRemark(remark);
        return vo;
    }
}
