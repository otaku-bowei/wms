package com.wms.base.domain.query;

import com.wms.common.core.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SKU 查询条件
 *
 * @author WMS
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SkuQuery extends PageQuery {

    /** 关键字（编码 / 名称 / 条形码） */
    private String keyword;

    /** SKU 编码（精确） */
    private String skuCode;

    /** SPU 编码 */
    private String spuCode;

    /** 类目 ID */
    private Long categoryId;

    /** 状态：1 启用 0 停用 */
    private Integer status;
}
