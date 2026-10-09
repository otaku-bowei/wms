package com.wms.base.domain.query;

import com.wms.common.core.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商查询条件
 *
 * @author WMS
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SupplierQuery extends PageQuery {

    /** 关键字（编码 / 名称） */
    private String keyword;

    /** 状态：1 启用 0 停用 */
    private Integer status;
}
