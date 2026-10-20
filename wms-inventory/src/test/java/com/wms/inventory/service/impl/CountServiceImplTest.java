package com.wms.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.inventory.domain.entity.CountItem;
import com.wms.inventory.domain.entity.CountOrder;
import com.wms.inventory.domain.entity.InvStock;
import com.wms.inventory.mapper.CountItemMapper;
import com.wms.inventory.mapper.CountOrderMapper;
import com.wms.inventory.mapper.InvStockMapper;
import com.wms.inventory.service.AdjustService;
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
 * 盘点服务单元测试：创建/录入实盘/生成差异复用调整单。
 */
@ExtendWith(MockitoExtension.class)
class CountServiceImplTest {

    @Mock
    private CountOrderMapper countOrderMapper;

    @Mock
    private CountItemMapper countItemMapper;

    @Mock
    private InvStockMapper stockMapper;

    @Mock
    private AdjustService adjustService;

    @InjectMocks
    private CountServiceImpl countService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(countService, "baseMapper", countOrderMapper);
    }

    @Test
    @DisplayName("创建盘点单：按系统库存回填 systemQty")
    void createCountOrder_shouldFillSystemQty() {
        CountOrder order = new CountOrder();
        order.setWarehouseId(1L);
        CountItem item = new CountItem();
        item.setSkuId(10L);
        item.setLocationId(20L);
        item.setBatchNo("B1");
        InvStock stock = new InvStock();
        stock.setTotalQty(10);
        when(stockMapper.selectOne(any(Wrapper.class))).thenReturn(stock);

        countService.createCountOrder(order, List.of(item));

        assertThat(order.getStatus()).isEqualTo("PENDING");
        assertThat(order.getCountNo()).startsWith("CN");
        ArgumentCaptor<CountItem> captor = ArgumentCaptor.forClass(CountItem.class);
        verify(countItemMapper).insert(captor.capture());
        assertThat(captor.getValue().getSystemQty()).isEqualTo(10);
    }

    @Test
    @DisplayName("录入实盘：回写差异并置订单 COUNTING")
    void recordCount_shouldComputeDiff() {
        CountItem item = new CountItem();
        item.setId(1L);
        item.setCountId(2L);
        item.setSystemQty(10);
        item.setStatus("PENDING");
        CountOrder order = new CountOrder();
        order.setId(2L);
        order.setStatus("PENDING");
        when(countItemMapper.selectById(1L)).thenReturn(item);
        when(countOrderMapper.selectById(2L)).thenReturn(order);

        countService.recordCount(1L, 8);

        ArgumentCaptor<CountItem> itemCaptor = ArgumentCaptor.forClass(CountItem.class);
        verify(countItemMapper).updateById(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getDiffQty()).isEqualTo(-2);
        assertThat(itemCaptor.getValue().getStatus()).isEqualTo("COUNTED");

        ArgumentCaptor<CountOrder> orderCaptor = ArgumentCaptor.forClass(CountOrder.class);
        verify(countOrderMapper).updateById(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo("COUNTING");
    }

    @Test
    @DisplayName("录入实盘：非 PENDING 明细抛出 25002")
    void recordCount_shouldThrow_whenNotPending() {
        CountItem item = new CountItem();
        item.setId(1L);
        item.setStatus("COUNTED");
        when(countItemMapper.selectById(1L)).thenReturn(item);

        BizException ex = assertThrows(BizException.class, () -> countService.recordCount(1L, 8));
        assertThat(ex.getCode()).isEqualTo(25002);
    }

    @Test
    @DisplayName("生成差异：创建并审批调整单，订单置 COMPLETED")
    void generateDiff_shouldCreateAdjust() {
        CountOrder order = new CountOrder();
        order.setId(2L);
        order.setWarehouseId(1L);
        order.setOperator("op1");
        order.setStatus("COUNTING");
        CountItem item = new CountItem();
        item.setId(3L);
        item.setCountId(2L);
        item.setSkuId(10L);
        item.setLocationId(20L);
        item.setBatchNo("B1");
        item.setSystemQty(10);
        item.setCountedQty(12);
        item.setDiffQty(2);
        item.setStatus("COUNTED");
        when(countOrderMapper.selectById(2L)).thenReturn(order);
        when(countItemMapper.selectList(any(Wrapper.class))).thenReturn(List.of(item));
        when(adjustService.createAdjust(any(), any())).thenReturn(99L);

        countService.generateDiff(2L);

        verify(adjustService).createAdjust(any(), any());
        verify(adjustService).audit(99L, true, "op1");
        ArgumentCaptor<CountOrder> orderCaptor = ArgumentCaptor.forClass(CountOrder.class);
        verify(countOrderMapper).updateById(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("生成差异：已完成订单抛出 25002")
    void generateDiff_shouldThrow_whenCompleted() {
        CountOrder order = new CountOrder();
        order.setId(2L);
        order.setStatus("COMPLETED");
        when(countOrderMapper.selectById(2L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class, () -> countService.generateDiff(2L));
        assertThat(ex.getCode()).isEqualTo(25002);
        verify(adjustService, never()).createAdjust(any(), any());
    }
}
