package com.wms.base.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库位批量创建参数
 *
 * @author WMS
 */
@Data
public class LocationBatchDTO {

    /** 仓库 ID */
    @NotNull(message = "所属仓库不能为空")
    private Long warehouseId;

    /** 区域 ID */
    @NotNull(message = "所属区域不能为空")
    private Long zoneId;

    /** 货架数量 */
    @NotNull(message = "货架数量不能为空")
    private Integer shelfCount;

    /** 每货架层数 */
    @NotNull(message = "每货架层数不能为空")
    private Integer layerCount;

    /** 每层列数 */
    @NotNull(message = "每层列数不能为空")
    private Integer columnCount;

    /** 每列位数 */
    @NotNull(message = "每列位数不能为空")
    private Integer positionCount;

    /** 库位类型 */
    private String locationType = "NORMAL";

    /** 统一最大承重（kg） */
    private BigDecimal maxWeight;

    /** 统一最大体积（m³） */
    private BigDecimal maxVolume;
}
