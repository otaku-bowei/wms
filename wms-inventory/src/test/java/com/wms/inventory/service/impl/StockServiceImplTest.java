package com.wms.inventory.service.impl;

import com.wms.api.inventory.dto.StockChangeDTO;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.inventory.domain.entity.InvStock;
import com.wms.inventory.domain.entity.InvTransaction;
import com.wms.inventory.mapper.InvStockMapper;
import com.wms.inventory.mapper.InvTransactionMapper;
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
 * 库存服务单元测试：增减/锁定/解锁/流水。
 */
@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock
    private InvStockMapper stockMapper;

    @Mock
    private InvTransactionMapper transactionMapper;

    @InjectMocks
    private StockServiceImpl stockService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stockService, "baseMapper", stockMapper);
    }

    private StockChangeDTO dto(int qty) {
        StockChangeDTO d = new StockChangeDTO();
        d.setSkuId(10L);
        d.setLocationId(20L);
        d.setBatchNo("B1");
        d.setQuantity(qty);
        d.setRelatedNo("REL");
        return d;
    }

    @Test
    @DisplayName("increase：在已有库存上增加可用量与总量")
    void increase_shouldAddOnExisting() {
        InvStock stock = new InvStock();
        stock.setAvailableQty(10);
        stock.setTotalQty(10);
        when(stockMapper.selectOne(any())).thenReturn(stock);

        stockService.increase(dto(5));

        ArgumentCaptor<InvStock> captor = ArgumentCaptor.forClass(InvStock.class);
        verify(stockMapper).updateById(captor.capture());
        assertThat(captor.getValue().getAvailableQty()).isEqualTo(15);
        assertThat(captor.getValue().getTotalQty()).isEqualTo(15);
        verify(transactionMapper).insert(any(InvTransaction.class));
    }

    @Test
    @DisplayName("increase：库存不存在时自动创建")
    void increase_shouldCreateWhenAbsent() {
        when(stockMapper.selectOne(any())).thenReturn(null);

        stockService.increase(dto(5));

        verify(stockMapper).insert(any(InvStock.class));
        ArgumentCaptor<InvStock> captor = ArgumentCaptor.forClass(InvStock.class);
        verify(stockMapper).updateById(captor.capture());
        assertThat(captor.getValue().getAvailableQty()).isEqualTo(5);
    }

    @Test
    @DisplayName("decrease：正常扣减可用量与总量")
    void decrease_shouldSubtract() {
        InvStock stock = new InvStock();
        stock.setAvailableQty(10);
        stock.setTotalQty(10);
        when(stockMapper.selectOne(any())).thenReturn(stock);

        stockService.decrease(dto(3));

        ArgumentCaptor<InvStock> captor = ArgumentCaptor.forClass(InvStock.class);
        verify(stockMapper).updateById(captor.capture());
        assertThat(captor.getValue().getAvailableQty()).isEqualTo(7);
        assertThat(captor.getValue().getTotalQty()).isEqualTo(7);
    }

    @Test
    @DisplayName("decrease：库存不足抛出 STOCK_NOT_ENOUGH")
    void decrease_shouldThrow_whenNotEnough() {
        InvStock stock = new InvStock();
        stock.setAvailableQty(2);
        stock.setTotalQty(2);
        when(stockMapper.selectOne(any())).thenReturn(stock);

        BizException ex = assertThrows(BizException.class, () -> stockService.decrease(dto(5)));
        assertThat(ex.getCode()).isEqualTo(ErrorCode.STOCK_NOT_ENOUGH.getCode());
        verify(stockMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("lock：预占可用量、增加锁定量")
    void lock_shouldReserve() {
        InvStock stock = new InvStock();
        stock.setAvailableQty(10);
        stock.setTotalQty(10);
        stock.setLockedQty(0);
        when(stockMapper.selectOne(any())).thenReturn(stock);

        stockService.lock(dto(4));

        ArgumentCaptor<InvStock> captor = ArgumentCaptor.forClass(InvStock.class);
        verify(stockMapper).updateById(captor.capture());
        assertThat(captor.getValue().getAvailableQty()).isEqualTo(6);
        assertThat(captor.getValue().getLockedQty()).isEqualTo(4);
    }

    @Test
    @DisplayName("unlock：释放锁定量、回补可用量")
    void unlock_shouldRelease() {
        InvStock stock = new InvStock();
        stock.setAvailableQty(6);
        stock.setTotalQty(10);
        stock.setLockedQty(4);
        when(stockMapper.selectOne(any())).thenReturn(stock);

        stockService.unlock(dto(4));

        ArgumentCaptor<InvStock> captor = ArgumentCaptor.forClass(InvStock.class);
        verify(stockMapper).updateById(captor.capture());
        assertThat(captor.getValue().getAvailableQty()).isEqualTo(10);
        assertThat(captor.getValue().getLockedQty()).isEqualTo(0);
    }
}
