package com.wms.base.domain.query;

import com.wms.common.core.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库位查询条件
 *
 * @author WMS
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LocationQuery extends PageQuery {

    /** 仓库 ID */
    private Long warehouseId;

    /** 区域 ID */
    private Long zoneId;

    /** 库位编码（模糊） */
    private String locationCode;

    /** 库位类型 */
    private String locationType;

    /** 状态：FREE / OCCUPIED / LOCKED / MAINTENANCE */
    private String status;

    /** 当前存放 SKU 编码 */
    private String skuCode;
}
