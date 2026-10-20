package com.wms.returned.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.wms.api.inventory.client.StockFeignClient;
import com.wms.api.inventory.dto.StockChangeDTO;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.core.result.R;
import com.wms.returned.domain.entity.ReturnItem;
import com.wms.returned.domain.entity.ReturnOrder;
import com.wms.returned.mapper.ReturnItemMapper;
import com.wms.returned.mapper.ReturnOrderMapper;
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
 * 退货服务单元测试：创建/收货/质检分级/重新上架（A 级回写库存）。
 */
@ExtendWith(MockitoExtension.class)
class ReturnServiceImplTest {

    @Mock
    private ReturnOrderMapper returnOrderMapper;

    @Mock
    private ReturnItemMapper returnItemMapper;

    @Mock
    private StockFeignClient stockFeignClient;

    @InjectMocks
    private ReturnServiceImpl returnService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(returnService, "baseMapper", returnOrderMapper);
    }

    @Test
    @DisplayName("创建退货单：状态 CREATED、单号 RN 开头")
    void createReturn_shouldBuildCreated() {
        ReturnOrder order = new ReturnOrder();
        order.setWarehouseId(1L);
        ReturnItem item = new ReturnItem();
        item.setSkuId(10L);
        item.setPlanQty(5);

        returnService.createReturn(order, List.of(item));

        assertThat(order.getStatus()).isEqualTo("CREATED");
        assertThat(order.getReturnNo()).startsWith("RN");
        ArgumentCaptor<ReturnItem> captor = ArgumentCaptor.forClass(ReturnItem.class);
        verify(returnItemMapper).insert(captor.capture());
        assertThat(captor.getValue().getReceivedQty()).isEqualTo(0);
    }

    @Test
    @DisplayName("退货收货：CREATED → RECEIVED，实收=计划")
    void receive_shouldSetReceived() {
        ReturnOrder order = new ReturnOrder();
        order.setId(1L);
        order.setStatus("CREATED");
        ReturnItem item = new ReturnItem();
        item.setId(2L);
        item.setReturnId(1L);
        item.setPlanQty(5);
        when(returnOrderMapper.selectById(1L)).thenReturn(order);
        when(returnItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(item));

        returnService.receive(1L);

        ArgumentCaptor<ReturnItem> itemCaptor = ArgumentCaptor.forClass(ReturnItem.class);
        verify(returnItemMapper).updateById(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getReceivedQty()).isEqualTo(5);
        ArgumentCaptor<ReturnOrder> orderCaptor = ArgumentCaptor.forClass(ReturnOrder.class);
        verify(returnOrderMapper).updateById(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo("RECEIVED");
    }

    @Test
    @DisplayName("退货收货：非 CREATED 抛出 26002")
    void receive_shouldThrow_whenNotCreated() {
        ReturnOrder order = new ReturnOrder();
        order.setId(1L);
        order.setStatus("RECEIVED");
        when(returnOrderMapper.selectById(1L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class, () -> returnService.receive(1L));
        assertThat(ex.getCode()).isEqualTo(26002);
    }

    @Test
    @DisplayName("质检分级：非法等级抛出 26003")
    void inspect_shouldThrow_whenInvalidGrade() {
        ReturnOrder order = new ReturnOrder();
        order.setId(1L);
        order.setStatus("RECEIVED");
        when(returnOrderMapper.selectById(1L)).thenReturn(order);
        ReturnItem graded = new ReturnItem();
        graded.setId(2L);
        graded.setGrade("X");

        BizException ex = assertThrows(BizException.class, () -> returnService.inspect(1L, List.of(graded)));
        assertThat(ex.getCode()).isEqualTo(26003);
    }

    @Test
    @DisplayName("重新上架：A 级回写库存并置 RESHELVED")
    void reshelve_shouldIncreaseStock_forGradeA() {
        ReturnOrder order = new ReturnOrder();
        order.setId(1L);
        order.setReturnNo("RN1");
        order.setStatus("INSPECTED");
        ReturnItem item = new ReturnItem();
        item.setId(2L);
        item.setReturnId(1L);
        item.setSkuId(10L);
        item.setLocationId(20L);
        item.setGrade("A");
        item.setReceivedQty(5);
        when(returnOrderMapper.selectById(1L)).thenReturn(order);
        when(returnItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(item));
        when(stockFeignClient.increase(any(StockChangeDTO.class))).thenReturn(R.ok());

        returnService.reshelve(1L);

        verify(stockFeignClient).increase(any(StockChangeDTO.class));
        ArgumentCaptor<ReturnOrder> orderCaptor = ArgumentCaptor.forClass(ReturnOrder.class);
        verify(returnOrderMapper).updateById(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo("RESHELVED");
    }

    @Test
    @DisplayName("重新上架：库存回写失败抛出 SYSTEM_ERROR")
    void reshelve_shouldThrow_whenIncreaseFail() {
        ReturnOrder order = new ReturnOrder();
        order.setId(1L);
        order.setReturnNo("RN1");
        order.setStatus("INSPECTED");
        ReturnItem item = new ReturnItem();
        item.setId(2L);
        item.setReturnId(1L);
        item.setSkuId(10L);
        item.setLocationId(20L);
        item.setGrade("A");
        item.setReceivedQty(5);
        when(returnOrderMapper.selectById(1L)).thenReturn(order);
        when(returnItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(item));
        when(stockFeignClient.increase(any(StockChangeDTO.class))).thenReturn(R.fail(ErrorCode.SYSTEM_ERROR));

        BizException ex = assertThrows(BizException.class, () -> returnService.reshelve(1L));
        assertThat(ex.getCode()).isEqualTo(ErrorCode.SYSTEM_ERROR.getCode());
    }
}
