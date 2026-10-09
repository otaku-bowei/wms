package com.wms.system.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象
 *
 * @author WMS
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private Long roleId;

    /** 角色编码 */
    private String roleCode;

    /** 角色名称 */
    private String roleName;

    private Long warehouseId;

    /** 仓库名称 */
    private String warehouseName;

    private String phone;
    private String email;

    /** 状态：1 启用 0 停用 */
    private Integer status;

    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
}
