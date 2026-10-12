package com.wms.outbound.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.wms.api.inventory.client.StockFeignClient;
import com.wms.api.inventory.dto.StockChangeDTO;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.core.result.R;
import com.wms.outbound.domain.OutboundStatus;
import com.wms.outbound.domain.entity.OutboundItem;
import com.wms.outbound.domain.entity.OutboundOrder;
import com.wms.outbound.mapper.OutboundItemMapper;
import com.wms.outbound.mapper.OutboundOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 出库单服务单元测试：覆盖创建/分配库存(锁定)/取消(释放)/状态校验。
 */
@ExtendWith(MockitoExtension.class)
class OutboundServiceImplTest {

    @Mock
    private OutboundOrderMapper orderMapper;

    @Mock
    private OutboundItemMapper itemMapper;

    @Mock
    private StockFeignClient stockFeignClient;

    @InjectMocks
    private OutboundServiceImpl outboundService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(outboundService, "baseMapper", orderMapper);
    }

    @Test
    @DisplayName("创建出库单：生成单号、状态 DRAFT、写入明细")
    void createOrder_shouldBuildDraft() {
        OutboundOrder order = new OutboundOrder();
        order.setWarehouseId(1L);
        OutboundItem item = new OutboundItem();
        item.setSkuId(10L);
        item.setPlanQty(5);

        Long id = outboundService.createOrder(order, List.of(item));

        assertThat(id).isNull(); // 单元测试下 insert 被 mock，ASSIGN_ID 不生效
        assertThat(order.getStatus()).isEqualTo(OutboundStatus.DRAFT);
        assertThat(order.getOutboundNo()).startsWith("OUT");
        ArgumentCaptor<OutboundItem> captor = ArgumentCaptor.forClass(OutboundItem.class);
        verify(itemMapper).insert(captor.capture());
        assertThat(captor.getValue().getOutboundId()).isNull();
        assertThat(captor.getValue().getPlanQty()).isEqualTo(5);
    }

    @Test
    @DisplayName("分配库存：DRAFT→ALLOCATED 并锁定每个明细库存")
    void allocate_shouldLockStockAndAllocate() {
        OutboundOrder order = buildOrder(1L, OutboundStatus.DRAFT);
        OutboundItem item = buildItem(1L, 10L, 20L, "B1", 5);
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(itemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(item));
        when(stockFeignClient.lock(any(StockChangeDTO.class))).thenReturn(R.ok());

        outboundService.allocate(1L);

        verify(stockFeignClient).lock(any(StockChangeDTO.class));
        ArgumentCaptor<OutboundOrder> captor = ArgumentCaptor.forClass(OutboundOrder.class);
        verify(orderMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OutboundStatus.ALLOCATED);
    }

    @Test
    @DisplayName("分配库存：锁定失败抛出 24004")
    void allocate_shouldThrow_whenLockFail() {
        OutboundOrder order = buildOrder(1L, OutboundStatus.DRAFT);
        OutboundItem item = buildItem(1L, 10L, 20L, "B1", 5);
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(itemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(item));
        when(stockFeignClient.lock(any(StockChangeDTO.class))).thenReturn(R.fail(ErrorCode.OUTBOUND_STOCK_LOCK_FAIL));

        BizException ex = assertThrows(BizException.class, () -> outboundService.allocate(1L));
        assertThat(ex.getCode()).isEqualTo(24004);
    }

    @Test
    @DisplayName("分配库存：非 DRAFT 状态抛出 24003")
    void allocate_shouldThrow_whenStatusInvalid() {
        when(orderMapper.selectById(1L)).thenReturn(buildOrder(1L, OutboundStatus.ALLOCATED));

        BizException ex = assertThrows(BizException.class, () -> outboundService.allocate(1L));
        assertThat(ex.getCode()).isEqualTo(24003);
    }

    @Test
    @DisplayName("取消：ALLOCATED 释放锁定并置 CANCELLED")
    void cancel_shouldUnlock_whenAllocated() {
        OutboundOrder order = buildOrder(1L, OutboundStatus.ALLOCATED);
        OutboundItem item = buildItem(1L, 10L, 20L, "B1", 5);
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(itemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(item));

        outboundService.cancel(1L);

        verify(stockFeignClient).unlock(any(StockChangeDTO.class));
        ArgumentCaptor<OutboundOrder> captor = ArgumentCaptor.forClass(OutboundOrder.class);
        verify(orderMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OutboundStatus.CANCELLED);
    }

    @Test
    @DisplayName("取消：DRAFT 直接置 CANCELLED，不调用释放")
    void cancel_shouldCancelDirectly_whenDraft() {
        when(orderMapper.selectById(1L)).thenReturn(buildOrder(1L, OutboundStatus.DRAFT));

        outboundService.cancel(1L);

        verify(stockFeignClient, never()).unlock(any(StockChangeDTO.class));
        verify(orderMapper).updateById(any(OutboundOrder.class));
    }

    @Test
    @DisplayName("取消：终态(已出库)抛出 24003")
    void cancel_shouldThrow_whenTerminal() {
        when(orderMapper.selectById(1L)).thenReturn(buildOrder(1L, OutboundStatus.SHIPPED));

        BizException ex = assertThrows(BizException.class, () -> outboundService.cancel(1L));
        assertThat(ex.getCode()).isEqualTo(24003);
    }

    private OutboundOrder buildOrder(Long id, String status) {
        OutboundOrder order = new OutboundOrder();
        order.setId(id);
        order.setOutboundNo("OUT" + id);
        order.setWarehouseId(1L);
        order.setStatus(status);
        return order;
    }

    private OutboundItem buildItem(Long outboundId, Long skuId, Long locationId, String batchNo, int planQty) {
        OutboundItem item = new OutboundItem();
        item.setId(100L);
        item.setOutboundId(outboundId);
        item.setSkuId(skuId);
        item.setLocationId(locationId);
        item.setBatchNo(batchNo);
        item.setPlanQty(planQty);
        return item;
    }
}
