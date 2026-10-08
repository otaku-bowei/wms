package com.wms.auth.domain.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应
 *
 * @author WMS
 */
@Data
@Builder
public class LoginVO {

    /** JWT Token */
    private String token;

    /** 有效期（秒） */
    private Long expiresIn;

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

    /** 是否强制修改密码 */
    private Boolean forceChangePassword;
}
