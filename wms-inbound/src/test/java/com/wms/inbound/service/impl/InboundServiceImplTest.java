package com.wms.inbound.service.impl;

import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.inbound.domain.InboundStatus;
import com.wms.inbound.domain.entity.InboundItem;
import com.wms.inbound.domain.entity.InboundOrder;
import com.wms.inbound.mapper.InboundItemMapper;
import com.wms.inbound.mapper.InboundOrderMapper;
import com.wms.inbound.service.InboundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 入库单服务单元测试：状态机驱动流转。
 */
@ExtendWith(MockitoExtension.class)
class InboundServiceImplTest {

    @Mock
    private InboundOrderMapper inboundOrderMapper;

    @Mock
    private InboundItemMapper inboundItemMapper;

    @InjectMocks
    private InboundServiceImpl inboundService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(inboundService, "baseMapper", inboundOrderMapper);
    }

    @Test
    @DisplayName("createOrder：置 DRAFT、单号 IN 开头、写入明细")
    void createOrder_shouldBuildDraft() {
        InboundOrder order = new InboundOrder();
        order.setWarehouseId(1L);
        InboundItem item = new InboundItem();
        item.setSkuId(10L);

        inboundService.createOrder(order, List.of(item));

        assertThat(order.getStatus()).isEqualTo(InboundStatus.DRAFT);
        assertThat(order.getInboundNo()).startsWith("IN");
        verify(inboundItemMapper).insert(any(InboundItem.class));
    }

    @Test
    @DisplayName("confirm：DRAFT → CONFIRMED")
    void confirm_shouldTransitionToConfirmed() {
        InboundOrder order = new InboundOrder();
        order.setId(1L);
        order.setStatus(InboundStatus.DRAFT);
        when(inboundOrderMapper.selectById(1L)).thenReturn(order);

        inboundService.confirm(1L);

        ArgumentCaptor<InboundOrder> captor = ArgumentCaptor.forClass(InboundOrder.class);
        verify(inboundOrderMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InboundStatus.CONFIRMED);
    }

    @Test
    @DisplayName("arrive：CONFIRMED → ARRIVED 并记录到货时间")
    void arrive_shouldTransitionToArrived() {
        InboundOrder order = new InboundOrder();
        order.setId(1L);
        order.setStatus(InboundStatus.CONFIRMED);
        when(inboundOrderMapper.selectById(1L)).thenReturn(order);

        inboundService.arrive(1L);

        ArgumentCaptor<InboundOrder> captor = ArgumentCaptor.forClass(InboundOrder.class);
        verify(inboundOrderMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InboundStatus.ARRIVED);
        assertThat(captor.getValue().getArriveTime()).isNotNull();
    }

    @Test
    @DisplayName("submitQc：ARRIVED → PENDING_QC")
    void submitQc_shouldTransitionToPendingQc() {
        InboundOrder order = new InboundOrder();
        order.setId(1L);
        order.setStatus(InboundStatus.ARRIVED);
        when(inboundOrderMapper.selectById(1L)).thenReturn(order);

        inboundService.submitQc(1L);

        ArgumentCaptor<InboundOrder> captor = ArgumentCaptor.forClass(InboundOrder.class);
        verify(inboundOrderMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InboundStatus.PENDING_QC);
    }

    @Test
    @DisplayName("confirm：从非 DRAFT 状态流转抛出 INBOUND_STATUS_INVALID")
    void confirm_shouldThrow_whenInvalidTransition() {
        InboundOrder order = new InboundOrder();
        order.setId(1L);
        order.setStatus(InboundStatus.ARRIVED);
        when(inboundOrderMapper.selectById(1L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class, () -> inboundService.confirm(1L));
        assertThat(ex.getCode()).isEqualTo(ErrorCode.INBOUND_STATUS_INVALID.getCode());
    }

    @Test
    @DisplayName("confirm：单据不存在抛出 INBOUND_NOT_FOUND")
    void confirm_shouldThrow_whenNotFound() {
        when(inboundOrderMapper.selectById(99L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> inboundService.confirm(99L));
        assertThat(ex.getCode()).isEqualTo(ErrorCode.INBOUND_NOT_FOUND.getCode());
    }
}
