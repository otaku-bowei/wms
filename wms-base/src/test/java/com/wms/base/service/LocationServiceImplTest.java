package com.wms.base.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.wms.base.domain.dto.LocationBatchDTO;
import com.wms.base.domain.dto.LocationCreateDTO;
import com.wms.base.domain.entity.BaseLocation;
import com.wms.base.domain.entity.BaseWarehouse;
import com.wms.base.domain.entity.BaseZone;
import com.wms.base.domain.vo.LocationLayoutVO;
import com.wms.base.domain.vo.LocationCellVO;
import com.wms.base.mapper.BaseLocationMapper;
import com.wms.base.mapper.BaseWarehouseMapper;
import com.wms.base.mapper.BaseZoneMapper;
import com.wms.base.service.impl.LocationServiceImpl;
import com.wms.common.core.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 库位服务单元测试
 *
 * <p>覆盖测试点：TP-R1-1.5.1-01 ~ TP-R1-1.5.4-09、TP-R1-1.5.5-01 ~ 02
 *
 * @author WMS
 */
@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private BaseLocationMapper locationMapper;

    @Mock
    private BaseWarehouseMapper warehouseMapper;

    @Mock
    private BaseZoneMapper zoneMapper;

    private LocationServiceImpl locationService;

    @BeforeEach
    void setUp() {
        locationService = new LocationServiceImpl(warehouseMapper, zoneMapper);
        ReflectionTestUtils.setField(locationService, "baseMapper", locationMapper);
        ReflectionTestUtils.setField(locationService, "batchLimit", 5000);
        ReflectionTestUtils.setField(locationService, "batchSize", 500);
    }

    @Test
    @DisplayName("新增库位：按规则生成编码 WH-001-A-01-01-01")
    void createLocation_shouldGenerateCode() {
        when(warehouseMapper.selectById(1L)).thenReturn(buildWarehouse("WH-001"));
        when(zoneMapper.selectById(1L)).thenReturn(buildZone("A"));
        when(locationMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        locationService.createLocation(buildCreateDTO());

        ArgumentCaptor<BaseLocation> captor = ArgumentCaptor.forClass(BaseLocation.class);
        verify(locationMapper).insert(captor.capture());
        assertThat(captor.getValue().getLocationCode()).isEqualTo("WH-001-A-01-01-01-01");
        assertThat(captor.getValue().getStatus()).isEqualTo("FREE");
    }

    @Test
    @DisplayName("库位编码已存在：抛出 40001")
    void createLocation_shouldThrow_whenCodeExists() {
        when(warehouseMapper.selectById(1L)).thenReturn(buildWarehouse("WH-001"));
        when(zoneMapper.selectById(1L)).thenReturn(buildZone("A"));
        when(locationMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        BizException ex = assertThrows(BizException.class,
                () -> locationService.createLocation(buildCreateDTO()));

        assertThat(ex.getCode()).isEqualTo(40001);
    }

    @Test
    @DisplayName("仓库不存在：抛出 30004")
    void createLocation_shouldThrow_whenWarehouseNotExists() {
        when(warehouseMapper.selectById(1L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> locationService.createLocation(buildCreateDTO()));

        assertThat(ex.getCode()).isEqualTo(30004);
    }

    @Test
    @DisplayName("批量创建：超出上限抛出 40004")
    void batchCreate_shouldThrow_whenExceedLimit() {
        LocationBatchDTO dto = buildBatchDTO(100, 10, 10, 10);

        BizException ex = assertThrows(BizException.class, () -> locationService.batchCreate(dto));

        assertThat(ex.getCode()).isEqualTo(40004);
    }

    @Test
    @DisplayName("批量创建：规格数量为 0 时抛出 400")
    void batchCreate_shouldThrow_whenTotalZero() {
        LocationBatchDTO dto = buildBatchDTO(0, 0, 0, 0);

        BizException ex = assertThrows(BizException.class, () -> locationService.batchCreate(dto));

        assertThat(ex.getCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("占用库位不可直接置为空闲：抛出 40005")
    void changeStatus_shouldThrow_whenOccupiedToFree() {
        BaseLocation occupied = new BaseLocation();
        occupied.setId(1L);
        occupied.setStatus("OCCUPIED");
        when(locationMapper.selectById(1L)).thenReturn(occupied);

        BizException ex = assertThrows(BizException.class,
                () -> locationService.changeStatus(1L, "FREE", null));

        assertThat(ex.getCode()).isEqualTo(40005);
    }

    @Test
    @DisplayName("锁定空闲库位：更新状态与原因")
    void changeStatus_shouldLock_whenFree() {
        BaseLocation free = new BaseLocation();
        free.setId(1L);
        free.setStatus("FREE");
        when(locationMapper.selectById(1L)).thenReturn(free);

        locationService.changeStatus(1L, "LOCKED", "检修");

        ArgumentCaptor<BaseLocation> captor = ArgumentCaptor.forClass(BaseLocation.class);
        verify(locationMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("LOCKED");
        assertThat(captor.getValue().getLockReason()).isEqualTo("检修");
    }

    @Test
    @DisplayName("修改库位：最大承重不可低于已用承重")
    void updateLocation_shouldThrow_whenMaxWeightBelowUsed() {
        BaseLocation location = new BaseLocation();
        location.setId(1L);
        location.setUsedWeight(new BigDecimal("100"));
        when(locationMapper.selectById(1L)).thenReturn(location);

        BizException ex = assertThrows(BizException.class,
                () -> locationService.updateLocation(1L, "NORMAL", new BigDecimal("50"), null));

        assertThat(ex.getCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("2D 平面图：按使用率与状态计算颜色等级")
    void layout_shouldCalculateColorLevel() {
        when(zoneMapper.selectById(1L)).thenReturn(buildZone("A"));
        when(locationMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                buildLocation(1L, "WH-001-A-01-01-01", "FREE", 0, "100"),
                buildLocation(2L, "WH-001-A-01-01-02", "OCCUPIED", 96, "100"),
                buildLocation(3L, "WH-001-A-01-01-03", "LOCKED", 0, "100")));

        LocationLayoutVO layout = locationService.layout(1L, 1L, null);

        assertThat(layout.getCells()).hasSize(3);
        assertThat(layout.getCells().stream().map(LocationCellVO::getColorLevel).toList())
                .containsExactlyInAnyOrder("GREEN", "RED", "GRAY");
        assertThat(layout.getFreeCount()).isEqualTo(1);
        assertThat(layout.getOccupiedCount()).isEqualTo(1);
        assertThat(layout.getUnavailableCount()).isEqualTo(1);
    }

    /* ==================== 辅助方法 ==================== */

    private BaseWarehouse buildWarehouse(String code) {
        BaseWarehouse warehouse = new BaseWarehouse();
        warehouse.setId(1L);
        warehouse.setWarehouseCode(code);
        return warehouse;
    }

    private BaseZone buildZone(String zoneCode) {
        BaseZone zone = new BaseZone();
        zone.setId(1L);
        zone.setZoneCode(zoneCode);
        zone.setZoneName(zoneCode + " 区");
        return zone;
    }

    private LocationCreateDTO buildCreateDTO() {
        LocationCreateDTO dto = new LocationCreateDTO();
        dto.setWarehouseId(1L);
        dto.setZoneId(1L);
        dto.setShelfNo(1);
        dto.setLayerNo(1);
        dto.setColumnNo(1);
        dto.setPositionNo(1);
        dto.setLocationType("NORMAL");
        dto.setMaxWeight(new BigDecimal("500"));
        return dto;
    }

    private LocationBatchDTO buildBatchDTO(int s, int l, int c, int p) {
        LocationBatchDTO dto = new LocationBatchDTO();
        dto.setWarehouseId(1L);
        dto.setZoneId(1L);
        dto.setShelfCount(s);
        dto.setLayerCount(l);
        dto.setColumnCount(c);
        dto.setPositionCount(p);
        dto.setLocationType("NORMAL");
        return dto;
    }

    private BaseLocation buildLocation(Long id, String code, String status, int usedWeight, String maxWeight) {
        BaseLocation location = new BaseLocation();
        location.setId(id);
        location.setLocationCode(code);
        location.setWarehouseId(1L);
        location.setZoneId(1L);
        location.setShelfNo(1);
        location.setLayerNo(1);
        location.setColumnNo(1);
        location.setPositionNo(1);
        location.setStatus(status);
        location.setUsedWeight(new BigDecimal(usedWeight));
        location.setMaxWeight(new BigDecimal(maxWeight));
        return location;
    }
}
