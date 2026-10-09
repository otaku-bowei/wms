package com.wms.system.domain.query;

import com.wms.common.core.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询条件
 *
 * @author WMS
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends PageQuery {

    /** 关键字（用户名 / 姓名） */
    private String keyword;

    /** 角色 ID */
    private Long roleId;

    /** 所属仓库 ID */
    private Long warehouseId;

    /** 状态：1 启用 0 停用 */
    private Integer status;
}
