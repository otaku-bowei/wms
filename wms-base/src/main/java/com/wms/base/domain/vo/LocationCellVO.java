package com.wms.base.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库位平面图单元格
 *
 * @author WMS
 */
@Data
@Builder
public class LocationCellVO {

    /** 库位 ID */
    private Long locationId;

    /** 库位编码 */
    private String locationCode;

    /** 货架 */
    private Integer shelf;

    /** 层 */
    private Integer layer;

    /** 列 */
    private Integer columnNo;

    /** 状态 */
    private String status;

    /** 使用率（0-100） */
    private BigDecimal usageRate;

    /** 颜色等级：GREEN / YELLOW / RED / GRAY */
    private String colorLevel;
}
