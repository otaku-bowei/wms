package com.wms.outbound.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.outbound.domain.OutboundStatus;
import com.wms.outbound.domain.dto.PickConfirmVO;
import com.wms.outbound.domain.entity.OutboundItem;
import com.wms.outbound.domain.entity.OutboundOrder;
import com.wms.outbound.domain.entity.PickTask;
import com.wms.outbound.mapper.OutboundItemMapper;
import com.wms.outbound.mapper.OutboundOrderMapper;
import com.wms.outbound.mapper.PickTaskMapper;
import com.wms.outbound.mapper.WaveItemMapper;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 拣货服务单元测试：覆盖拣货确认回写、订单完成流转、数量/状态校验。
 */
@ExtendWith(MockitoExtension.class)
class PickServiceImplTest {

    @Mock
    private PickTaskMapper pickTaskMapper;

    @Mock
    private OutboundItemMapper outboundItemMapper;

    @Mock
    private OutboundOrderMapper outboundOrderMapper;

    @Mock
    private WaveItemMapper waveItemMapper;

    @InjectMocks
    private PickServiceImpl pickService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pickService, "baseMapper", pickTaskMapper);
    }

    @Test
    @DisplayName("拣货确认：回写已拣数量，全部完成后订单置 PICKED")
    void confirmPick_shouldCompleteOrder_whenAllPicked() {
        PickTask task = buildTask(1L, 2L, 3L, 5, 0, "PENDING");
        OutboundItem item = buildItem(3L, 2L, 0);
        OutboundOrder order = buildOrder(2L, OutboundStatus.PICKING);
        PickTask pickedTask = buildTask(1L, 2L, 3L, 5, 3, "PICKED");
        when(pickTaskMapper.selectById(1L)).thenReturn(task);
        when(outboundItemMapper.selectById(3L)).thenReturn(item);
        when(outboundOrderMapper.selectById(2L)).thenReturn(order);
        when(pickTaskMapper.selectList(any(Wrapper.class))).thenReturn(List.of(pickedTask));
        when(waveItemMapper.selectList(any(Wrapper.class))).thenReturn(Collections.emptyList());

        pickService.confirmPick(buildVo(1L, 3, 20L, "picker1", null));

        ArgumentCaptor<PickTask> taskCaptor = ArgumentCaptor.forClass(PickTask.class);
        verify(pickTaskMapper).updateById(taskCaptor.capture());
        assertThat(taskCaptor.getValue().getActualQty()).isEqualTo(3);
        assertThat(taskCaptor.getValue().getStatus()).isEqualTo("PICKED");

        ArgumentCaptor<OutboundItem> itemCaptor = ArgumentCaptor.forClass(OutboundItem.class);
        verify(outboundItemMapper).updateById(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getPickedQty()).isEqualTo(3);

        ArgumentCaptor<OutboundOrder> orderCaptor = ArgumentCaptor.forClass(OutboundOrder.class);
        verify(outboundOrderMapper).updateById(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OutboundStatus.PICKED);
    }

    @Test
    @DisplayName("拣货确认：数量超过待拣数量抛出 24006")
    void confirmPick_shouldThrow_whenQtyExceed() {
        PickTask task = buildTask(1L, 2L, 3L, 5, 0, "PENDING");
        when(pickTaskMapper.selectById(1L)).thenReturn(task);

        BizException ex = assertThrows(BizException.class,
                () -> pickService.confirmPick(buildVo(1L, 10, 20L, "p", null)));
        assertThat(ex.getCode()).isEqualTo(24006);
    }

    @Test
    @DisplayName("拣货确认：任务不存在抛出 24009")
    void confirmPick_shouldThrow_whenTaskNotFound() {
        when(pickTaskMapper.selectById(1L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> pickService.confirmPick(buildVo(1L, 3, 20L, "p", null)));
        assertThat(ex.getCode()).isEqualTo(24009);
    }

    @Test
    @DisplayName("拣货确认：非 PENDING 任务抛出 24003")
    void confirmPick_shouldThrow_whenTaskNotPending() {
        PickTask task = buildTask(1L, 2L, 3L, 5, 0, "PICKED");
        when(pickTaskMapper.selectById(1L)).thenReturn(task);

        BizException ex = assertThrows(BizException.class,
                () -> pickService.confirmPick(buildVo(1L, 3, 20L, "p", null)));
        assertThat(ex.getCode()).isEqualTo(24003);
    }

    private PickTask buildTask(Long id, Long outboundId, Long itemId, int planQty, int actualQty, String status) {
        PickTask task = new PickTask();
        task.setId(id);
        task.setOutboundId(outboundId);
        task.setItemId(itemId);
        task.setSkuId(10L);
        task.setWarehouseId(1L);
        task.setLocationId(20L);
        task.setPlanQty(planQty);
        task.setActualQty(actualQty);
        task.setStatus(status);
        return task;
    }

    private OutboundItem buildItem(Long id, Long outboundId, int pickedQty) {
        OutboundItem item = new OutboundItem();
        item.setId(id);
        item.setOutboundId(outboundId);
        item.setSkuId(10L);
        item.setLocationId(20L);
        item.setPlanQty(5);
        item.setPickedQty(pickedQty);
        return item;
    }

    private OutboundOrder buildOrder(Long id, String status) {
        OutboundOrder order = new OutboundOrder();
        order.setId(id);
        order.setStatus(status);
        return order;
    }

    private PickConfirmVO buildVo(Long taskId, int actualQty, Long locationId, String picker, String remark) {
        PickConfirmVO vo = new PickConfirmVO();
        vo.setTaskId(taskId);
        vo.setActualQty(actualQty);
        vo.setLocationId(locationId);
        vo.setPicker(picker);
        vo.setRemark(remark);
        return vo;
    }
}
