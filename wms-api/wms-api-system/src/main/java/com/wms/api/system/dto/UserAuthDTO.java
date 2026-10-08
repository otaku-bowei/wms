package com.wms.api.system.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 认证用用户信息（跨服务传输）
 *
 * @author WMS
 */
@Data
public class UserAuthDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 姓名 */
    private String realName;

    /** 密码密文 */
    private String password;

    /** 角色 ID */
    private Long roleId;

    /** 角色编码 */
    private String roleCode;

    /** 所属仓库 ID */
    private Long warehouseId;

    /** 状态：1 启用 0 停用 */
    private Integer status;

    /** 是否强制修改密码：1 是 0 否 */
    private Integer forceChangePassword;
}
