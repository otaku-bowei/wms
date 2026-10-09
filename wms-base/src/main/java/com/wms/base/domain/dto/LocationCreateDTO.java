package com.wms.base.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库位新增参数
 *
 * @author WMS
 */
@Data
public class LocationCreateDTO {

    /** 仓库 ID */
    @NotNull(message = "所属仓库不能为空")
    private Long warehouseId;

    /** 区域 ID */
    @NotNull(message = "所属区域不能为空")
    private Long zoneId;

    /** 货架号 */
    @NotNull(message = "货架号不能为空")
    private Integer shelfNo;

    /** 层号 */
    @NotNull(message = "层号不能为空")
    private Integer layerNo;

    /** 列号 */
    @NotNull(message = "列号不能为空")
    private Integer columnNo;

    /** 位号 */
    @NotNull(message = "位号不能为空")
    private Integer positionNo;

    /** 库位类型：NORMAL / TEMP / DEFECT / RETURN_PENDING */
    @NotNull(message = "库位类型不能为空")
    private String locationType;

    /** 最大承重（kg） */
    private BigDecimal maxWeight;

    /** 最大体积（m³） */
    private BigDecimal maxVolume;
}
