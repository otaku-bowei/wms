package com.wms.base.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 库位 2D 平面图
 *
 * @author WMS
 */
@Data
@Builder
public class LocationLayoutVO {

    /** 区域名称 */
    private String zoneName;

    /** 货架数 */
    private Integer shelfCount;

    /** 层数 */
    private Integer layerCount;

    /** 列数 */
    private Integer columnCount;

    /** 单元格列表 */
    private List<LocationCellVO> cells;

    /** 统计：空闲数量 */
    private long freeCount;

    /** 统计：占用数量 */
    private long occupiedCount;

    /** 统计：不可用数量（锁定 / 维修） */
    private long unavailableCount;
}
