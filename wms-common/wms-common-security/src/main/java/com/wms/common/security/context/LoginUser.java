package com.wms.common.security.context;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * 登录用户信息
 *
 * @author WMS
 */
@Data
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 姓名 */
    private String realName;

    /** 角色编码 */
    private String roleCode;

    /** 所属仓库 ID（可空） */
    private Long warehouseId;

    /** 权限标识集合 */
    private Set<String> permissions;

    /**
     * 是否拥有指定权限
     *
     * @param permissionCode 权限标识
     * @return true 拥有
     */
    public boolean hasPermission(String permissionCode) {
        return permissions != null && permissions.contains(permissionCode);
    }
}
