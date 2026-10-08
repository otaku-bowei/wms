package com.wms.auth.domain.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 当前用户信息
 *
 * @author WMS
 */
@Data
@Builder
public class UserInfoVO {

    /** 用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 姓名 */
    private String realName;

    /** 角色编码 */
    private String roleCode;

    /** 所属仓库 ID */
    private Long warehouseId;

    /** 所属仓库名称 */
    private String warehouseName;
}
