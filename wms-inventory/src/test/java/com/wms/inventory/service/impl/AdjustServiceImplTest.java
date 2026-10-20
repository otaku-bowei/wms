package com.wms.inventory.service.impl;

import com.wms.api.inventory.dto.StockChangeDTO;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.inventory.domain.entity.InvAdjust;
import com.wms.inventory.domain.entity.InvAdjustItem;
import com.wms.inventory.domain.entity.InvStock;
import com.wms.inventory.mapper.InvAdjustItemMapper;
import com.wms.inventory.mapper.InvAdjustMapper;
import com.wms.inventory.mapper.InvStockMapper;
import com.wms.inventory.service.AdjustService;
import com.wms.inventory.service.StockService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 库存调整单服务单元测试：创建/审批增减库存。
 */
@ExtendWith(MockitoExtension.class)
class AdjustServiceImplTest {

    @Mock
    private InvAdjustMapper adjustMapper;

    @Mock
    private InvAdjustItemMapper adjustItemMapper;

    @Mock
    private InvStockMapper stockMapper;

    @Mock
    private StockService stockService;

    @InjectMocks
    private AdjustServiceImpl adjustService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adjustService, "baseMapper", adjustMapper);
    }

    private InvAdjustItem item(int afterQty, int beforeQty) {
        InvAdjustItem it = new InvAdjustItem();
        it.setSkuId(10L);
        it.setLocationId(20L);
        it.setBatchNo("B1");
        it.setAfterQty(afterQty);
        return it;
    }

    @Test
    @DisplayName("createAdjust：置 DRAFT 并按系统库存计算差异")
    void createAdjust_shouldComputeDiff() {
        InvStock stock = new InvStock();
        stock.setAvailableQty(8);
        when(stockMapper.selectOne(any())).thenReturn(stock);

        InvAdjust adjust = new InvAdjust();
        adjust.setWarehouseId(1L);
        Long id = adjustService.createAdjust(adjust, List.of(item(10, 0)));

        assertThat(adjust.getStatus()).isEqualTo("DRAFT");
        assertThat(adjust.getAdjustNo()).startsWith("ADJ");
        ArgumentCaptor<InvAdjustItem> captor = ArgumentCaptor.forClass(InvAdjustItem.class);
        verify(adjustItemMapper).insert(captor.capture());
        assertThat(captor.getValue().getDiffQty()).isEqualTo(2); // 10 - 8
    }

    @Test
    @DisplayName("audit 通过且差异为正：调用库存增加")
    void audit_shouldIncrease_whenPositiveDiff() {
        InvAdjust adjust = new InvAdjust();
        adjust.setId(1L);
        adjust.setStatus("PENDING");
        when(adjustMapper.selectById(1L)).thenReturn(adjust);
        InvAdjustItem it = item(10, 0);
        it.setDiffQty(2);
        when(adjustItemMapper.selectList(any())).thenReturn(List.of(it));

        adjustService.audit(1L, true, "op");

        ArgumentCaptor<StockChangeDTO> captor = ArgumentCaptor.forClass(StockChangeDTO.class);
        verify(stockService).increase(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(2);
        ArgumentCaptor<InvAdjust> aCaptor = ArgumentCaptor.forClass(InvAdjust.class);
        verify(adjustMapper).updateById(aCaptor.capture());
        assertThat(aCaptor.getValue().getStatus()).isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("audit 通过且差异为负：调用库存扣减")
    void audit_shouldDecrease_whenNegativeDiff() {
        InvAdjust adjust = new InvAdjust();
        adjust.setId(1L);
        adjust.setStatus("PENDING");
        when(adjustMapper.selectById(1L)).thenReturn(adjust);
        InvAdjustItem it = item(5, 0);
        it.setDiffQty(-3);
        when(adjustItemMapper.selectList(any())).thenReturn(List.of(it));

        adjustService.audit(1L, true, "op");

        ArgumentCaptor<StockChangeDTO> captor = ArgumentCaptor.forClass(StockChangeDTO.class);
        verify(stockService).decrease(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("audit 驳回：置 REJECTED 且不改库存")
    void audit_shouldReject() {
        InvAdjust adjust = new InvAdjust();
        adjust.setId(1L);
        adjust.setStatus("PENDING");
        when(adjustMapper.selectById(1L)).thenReturn(adjust);

        adjustService.audit(1L, false, "op");

        ArgumentCaptor<InvAdjust> captor = ArgumentCaptor.forClass(InvAdjust.class);
        verify(adjustMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("REJECTED");
        verify(stockService, never()).increase(any());
        verify(stockService, never()).decrease(any());
    }

    @Test
    @DisplayName("audit 非 PENDING/DRAFT：抛出 ADJUST_STATUS_INVALID")
    void audit_shouldThrow_whenInvalidStatus() {
        InvAdjust adjust = new InvAdjust();
        adjust.setId(1L);
        adjust.setStatus("APPROVED");
        when(adjustMapper.selectById(1L)).thenReturn(adjust);

        BizException ex = assertThrows(BizException.class, () -> adjustService.audit(1L, true, "op"));
        assertThat(ex.getCode()).isEqualTo(ErrorCode.ADJUST_STATUS_INVALID.getCode());
    }
}
