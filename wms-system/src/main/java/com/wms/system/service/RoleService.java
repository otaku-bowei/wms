package com.wms.system.service;

import com.wms.api.system.dto.PermissionNodeDTO;
import com.wms.system.domain.entity.SysPermission;
import com.wms.system.domain.entity.SysRole;

import java.util.List;
import java.util.Set;

/**
 * 角色与权限服务接口
 *
 * @author WMS
 */
public interface RoleService {

    /**
     * 查询角色列表
     *
     * @return 角色列表
     */
    List<SysRole> listRoles();

    /**
     * 查询角色详情
     *
     * @param id 角色 ID
     * @return 角色
     */
    SysRole getById(Long id);

    /**
     * 修改角色描述
     *
     * @param id          角色 ID
     * @param roleName    角色名称
     * @param description 描述
     */
    void updateRole(Long id, String roleName, String description);

    /**
     * 查询权限树（全部菜单）
     *
     * @return 权限树
     */
    List<PermissionNodeDTO> getPermissionTree();

    /**
     * 查询用户权限标识集合
     *
     * @param userId 用户 ID
     * @return 权限标识集合
     */
    Set<String> getUserPermissionCodes(Long userId);

    /**
     * 查询用户菜单树
     *
     * @param userId 用户 ID
     * @return 菜单树
     */
    List<PermissionNodeDTO> getUserMenus(Long userId);

    /**
     * 查询角色权限标识集合
     *
     * @param roleId 角色 ID
     * @return 权限标识集合
     */
    Set<String> getRolePermissionCodes(Long roleId);

    /**
     * 配置角色权限
     *
     * @param roleId          角色 ID
     * @param permissionCodes 权限标识集合
     */
    void updateRolePermissions(Long roleId, List<String> permissionCodes);

    /**
     * 查询全部权限
     *
     * @return 权限列表
     */
    List<SysPermission> listAllPermissions();
}
