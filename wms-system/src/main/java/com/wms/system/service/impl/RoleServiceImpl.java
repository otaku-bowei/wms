package com.wms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.api.system.dto.PermissionNodeDTO;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.redis.constant.RedisKeys;
import com.wms.system.domain.entity.SysPermission;
import com.wms.system.domain.entity.SysRole;
import com.wms.system.domain.entity.SysRolePermission;
import com.wms.system.domain.entity.SysUser;
import com.wms.system.mapper.SysPermissionMapper;
import com.wms.system.mapper.SysRoleMapper;
import com.wms.system.mapper.SysRolePermissionMapper;
import com.wms.system.mapper.SysUserMapper;
import com.wms.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色与权限服务实现
 *
 * @author WMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements RoleService {

    private static final String TYPE_MENU = "MENU";

    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public List<SysRole> listRoles() {
        return this.list(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId));
    }

    @Override
    public SysRole getById(Long id) {
        // 调用 ServiceImpl 实现，避免使用 this 导致递归
        return super.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long id, String roleName, String description) {
        SysRole role = this.getById(id);
        if (role == null) {
            throw new BizException(ErrorCode.ROLE_NOT_FOUND);
        }
        role.setRoleName(roleName);
        role.setDescription(description);
        this.updateById(role);
    }

    @Override
    public List<SysPermission> listAllPermissions() {
        return permissionMapper.selectList(new LambdaQueryWrapper<SysPermission>().orderByAsc(SysPermission::getSort));
    }

    @Override
    public List<PermissionNodeDTO> getPermissionTree() {
        return buildTree(listAllPermissions());
    }

    @Override
    public Set<String> getUserPermissionCodes(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getRoleId() == null) {
            return Set.of();
        }
        return getRolePermissionCodes(user.getRoleId());
    }

    @Override
    public List<PermissionNodeDTO> getUserMenus(Long userId) {
        Set<String> codes = getUserPermissionCodes(userId);
        if (CollectionUtils.isEmpty(codes)) {
            return List.of();
        }
        List<SysPermission> menus = listAllPermissions().stream()
                .filter(p -> TYPE_MENU.equals(p.getPermissionType()))
                .filter(p -> codes.contains(p.getPermissionCode()))
                .toList();
        return buildTree(menus);
    }

    @Override
    public Set<String> getRolePermissionCodes(Long roleId) {
        List<SysRolePermission> relations = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        if (CollectionUtils.isEmpty(relations)) {
            return Set.of();
        }
        Set<Long> permissionIds = relations.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toSet());
        List<SysPermission> permissions = permissionMapper.selectBatchIds(permissionIds);
        return permissions.stream().map(SysPermission::getPermissionCode).collect(Collectors.toSet());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRolePermissions(Long roleId, List<String> permissionCodes) {
        SysRole role = this.getById(roleId);
        if (role == null) {
            throw new BizException(ErrorCode.ROLE_NOT_FOUND);
        }
        // 先删除该角色原有权限
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        if (CollectionUtils.isEmpty(permissionCodes)) {
            clearRoleUserPermissionCache(roleId);
            return;
        }
        // 按编码查询权限 ID
        List<SysPermission> permissions = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().in(SysPermission::getPermissionCode, permissionCodes));
        for (SysPermission permission : permissions) {
            SysRolePermission relation = new SysRolePermission();
            relation.setRoleId(roleId);
            relation.setPermissionId(permission.getId());
            rolePermissionMapper.insert(relation);
        }
        clearRoleUserPermissionCache(roleId);
    }

    /* ==================== 私有方法 ==================== */

    /**
     * 将权限列表组装为树形结构
     */
    private List<PermissionNodeDTO> buildTree(List<SysPermission> permissions) {
        if (CollectionUtils.isEmpty(permissions)) {
            return List.of();
        }
        Map<Long, PermissionNodeDTO> nodeMap = new LinkedHashMap<>();
        permissions.forEach(p -> nodeMap.put(p.getId(), toNode(p)));

        List<PermissionNodeDTO> roots = new ArrayList<>();
        for (SysPermission permission : permissions) {
            PermissionNodeDTO node = nodeMap.get(permission.getId());
            Long parentId = permission.getParentId();
            if (parentId == null || parentId == 0 || !nodeMap.containsKey(parentId)) {
                roots.add(node);
            } else {
                PermissionNodeDTO parent = nodeMap.get(parentId);
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    private PermissionNodeDTO toNode(SysPermission permission) {
        PermissionNodeDTO node = new PermissionNodeDTO();
        node.setId(permission.getId());
        node.setPermissionCode(permission.getPermissionCode());
        node.setPermissionName(permission.getPermissionName());
        node.setParentId(permission.getParentId());
        node.setPermissionType(permission.getPermissionType());
        node.setPath(permission.getPath());
        node.setIcon(permission.getIcon());
        node.setSort(permission.getSort());
        return node;
    }

    /**
     * 清理该角色下所有用户的权限缓存
     */
    private void clearRoleUserPermissionCache(Long roleId) {
        try {
            List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRoleId, roleId));
            if (!CollectionUtils.isEmpty(users)) {
                List<String> keys = users.stream()
                        .map(user -> RedisKeys.userPerms(user.getId()))
                        .toList();
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("清理角色用户权限缓存失败：roleId={}", roleId);
        }
    }
}
