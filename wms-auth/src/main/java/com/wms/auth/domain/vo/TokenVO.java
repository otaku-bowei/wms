package com.wms.auth.domain.vo;

import com.wms.api.system.dto.PermissionNodeDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 菜单权限响应
 *
 * @author WMS
 */
@Data
@Builder
public class TokenVO {

    /** 新 Token */
    private String token;

    /** 有效期（秒） */
    private Long expiresIn;

    /** 菜单树（兼容菜单查询场景） */
    private List<PermissionNodeDTO> menus;

    /** 按钮权限集合 */
    private Set<String> permissions;
}
