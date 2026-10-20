package com.wms.inbound.service.impl;

import com.wms.api.inventory.client.StockFeignClient;
import com.wms.api.inventory.dto.StockChangeDTO;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.core.result.R;
import com.wms.inbound.domain.InboundStatus;
import com.wms.inbound.domain.dto.PutawayConfirmVO;
import com.wms.inbound.domain.entity.InboundItem;
import com.wms.inbound.domain.entity.InboundOrder;
import com.wms.inbound.domain.entity.PutawayTask;
import com.wms.inbound.mapper.InboundItemMapper;
import com.wms.inbound.mapper.InboundOrderMapper;
import com.wms.inbound.mapper.PutawayTaskMapper;
import com.wms.inbound.service.PutawayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 上架服务单元测试：上架确认回写库存并驱动入库单完成。
 */
@ExtendWith(MockitoExtension.class)
class PutawayServiceImplTest {

    @Mock
    private PutawayTaskMapper putawayTaskMapper;

    @Mock
    private InboundItemMapper inboundItemMapper;

    @Mock
    private InboundOrderMapper inboundOrderMapper;

    @Mock
    private StockFeignClient stockFeignClient;

    @InjectMocks
    private PutawayServiceImpl putawayService;

    private PutawayTask pendingTask() {
        PutawayTask t = new PutawayTask();
        t.setId(1L);
        t.setInboundId(100L);
        t.setItemId(200L);
        t.setSkuId(10L);
        t.setLocationId(20L);
        t.setPlanQty(5);
        t.setActualQty(0);
        t.setStatus("PENDING");
        t.setDeleted(0);
        return t;
    }

    @Test
    @DisplayName("createTask：置 PENDING、单号 PW 开头")
    void createTask_shouldBuildPending() {
        PutawayTask task = new PutawayTask();
        task.setSkuId(10L);
        Long id = putawayService.createTask(task);
        assertThat(task.getStatus()).isEqualTo("PENDING");
        assertThat(task.getTaskNo()).startsWith("PW");
    }

    @Test
    @DisplayName("confirmPutaway：成功回写库存并驱动入库单完成")
    void confirmPutaway_shouldIncreaseStockAndCompleteOrder() {
        PutawayTask task = pendingTask();
        when(putawayTaskMapper.selectById(1L)).thenReturn(task);
        InboundItem item = new InboundItem();
        item.setId(200L);
        item.setInboundId(100L);
        item.setBatchNo("B1");
        item.setPlanQty(5);
        item.setPutawayQty(0);
        when(inboundItemMapper.selectById(200L)).thenReturn(item);
        InboundOrder order = new InboundOrder();
        order.setId(100L);
        order.setStatus(InboundStatus.PUTTING_AWAY);
        when(inboundOrderMapper.selectById(100L)).thenReturn(order);
        when(inboundItemMapper.selectList(any())).thenReturn(List.of(item));
        when(stockFeignClient.increase(any(StockChangeDTO.class))).thenReturn(R.ok());

        putawayService.confirmPutaway(new PutawayConfirmVO() {{
            setTaskId(1L);
            setActualQty(5);
            setOperator("op");
        }});

        verify(stockFeignClient).increase(any(StockChangeDTO.class));
        ArgumentCaptor<InboundOrder> orderCaptor = ArgumentCaptor.forClass(InboundOrder.class);
        verify(inboundOrderMapper).updateById(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(InboundStatus.COMPLETED);
    }

    @Test
    @DisplayName("confirmPutaway：上架量超过剩余量抛出 INBOUND_QTY_EXCEED")
    void confirmPutaway_shouldThrow_whenExceedRemaining() {
        PutawayTask task = pendingTask();
        task.setActualQty(3); // 剩余 2
        when(putawayTaskMapper.selectById(1L)).thenReturn(task);
        when(inboundItemMapper.selectById(200L)).thenReturn(new InboundItem());

        PutawayConfirmVO vo = new PutawayConfirmVO();
        vo.setTaskId(1L);
        vo.setActualQty(3);

        BizException ex = assertThrows(BizException.class, () -> putawayService.confirmPutaway(vo));
        assertThat(ex.getCode()).isEqualTo(ErrorCode.INBOUND_QTY_EXCEED.getCode());
        verify(stockFeignClient, never()).increase(any());
    }

    @Test
    @DisplayName("confirmPutaway：库存回写失败抛出 SYSTEM_ERROR")
    void confirmPutaway_shouldThrow_whenIncreaseFail() {
        PutawayTask task = pendingTask();
        when(putawayTaskMapper.selectById(1L)).thenReturn(task);
        when(inboundItemMapper.selectById(200L)).thenReturn(new InboundItem());
        when(stockFeignClient.increase(any(StockChangeDTO.class))).thenReturn(R.fail(ErrorCode.SYSTEM_ERROR));

        PutawayConfirmVO vo = new PutawayConfirmVO();
        vo.setTaskId(1L);
        vo.setActualQty(5);

        BizException ex = assertThrows(BizException.class, () -> putawayService.confirmPutaway(vo));
        assertThat(ex.getCode()).isEqualTo(ErrorCode.SYSTEM_ERROR.getCode());
    }

    @Test
    @DisplayName("confirmPutaway：任务不存在抛出 INBOUND_NOT_FOUND")
    void confirmPutaway_shouldThrow_whenTaskNotFound() {
        when(putawayTaskMapper.selectById(1L)).thenReturn(null);

        PutawayConfirmVO vo = new PutawayConfirmVO();
        vo.setTaskId(1L);
        vo.setActualQty(5);

        BizException ex = assertThrows(BizException.class, () -> putawayService.confirmPutaway(vo));
        assertThat(ex.getCode()).isEqualTo(ErrorCode.INBOUND_NOT_FOUND.getCode());
    }
}
