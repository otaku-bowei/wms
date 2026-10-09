package com.wms.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.wms.api.system.dto.PermissionNodeDTO;
import com.wms.common.core.exception.BizException;
import com.wms.system.domain.entity.SysPermission;
import com.wms.system.domain.entity.SysRole;
import com.wms.system.domain.entity.SysRolePermission;
import com.wms.system.domain.entity.SysUser;
import com.wms.system.mapper.SysPermissionMapper;
import com.wms.system.mapper.SysRoleMapper;
import com.wms.system.mapper.SysRolePermissionMapper;
import com.wms.system.mapper.SysUserMapper;
import com.wms.system.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 角色与权限服务单元测试
 *
 * <p>覆盖测试点：TP-R1-1.2.2-01 ~ TP-R1-1.2.3-05
 *
 * @author WMS
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    private static final Long ROLE_ID = 1L;
    private static final Long USER_ID = 1L;

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private SysPermissionMapper permissionMapper;

    @Mock
    private SysRolePermissionMapper rolePermissionMapper;

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    private RoleServiceImpl roleService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        // 手动构造以显式注入 ServiceImpl 的 baseMapper（Mockito 构造器注入不会填充父类 protected 字段）
        roleService = new RoleServiceImpl(permissionMapper, rolePermissionMapper, userMapper, redisTemplate);
        org.springframework.test.util.ReflectionTestUtils.setField(roleService, "baseMapper", roleMapper);
    }

    @Test
    @DisplayName("权限树：父子节点正确组装")
    void getPermissionTree_shouldBuildTree() {
        SysPermission root = buildPermission(1L, "MENU_BASIC", "基础数据", 0L, "MENU");
        SysPermission child = buildPermission(2L, "MENU_SKU", "SKU 管理", 1L, "MENU");
        when(permissionMapper.selectList(any(Wrapper.class))).thenReturn(List.of(root, child));

        List<PermissionNodeDTO> tree = roleService.getPermissionTree();

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getPermissionCode()).isEqualTo("MENU_BASIC");
        assertThat(tree.get(0).getChildren()).hasSize(1);
        assertThat(tree.get(0).getChildren().get(0).getPermissionCode()).isEqualTo("MENU_SKU");
    }

    @Test
    @DisplayName("查询用户权限集合：用户 -> 角色 -> 权限")
    void getUserPermissionCodes_shouldReturnCodes() {
        SysUser user = new SysUser();
        user.setId(USER_ID);
        user.setRoleId(ROLE_ID);
        when(userMapper.selectById(USER_ID)).thenReturn(user);
        SysRolePermission relation = new SysRolePermission();
        relation.setRoleId(ROLE_ID);
        relation.setPermissionId(1L);
        when(rolePermissionMapper.selectList(any(Wrapper.class))).thenReturn(List.of(relation));
        when(permissionMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(buildPermission(1L, "sku:view", "查询", 1L, "BUTTON")));

        Set<String> codes = roleService.getUserPermissionCodes(USER_ID);

        assertThat(codes).containsExactly("sku:view");
    }

    @Test
    @DisplayName("查询用户菜单：仅返回 MENU 类型且有权限的节点")
    void getUserMenus_shouldFilterMenuType() {
        SysUser user = new SysUser();
        user.setId(USER_ID);
        user.setRoleId(ROLE_ID);
        when(userMapper.selectById(USER_ID)).thenReturn(user);
        SysRolePermission relation = new SysRolePermission();
        relation.setRoleId(ROLE_ID);
        relation.setPermissionId(1L);
        when(rolePermissionMapper.selectList(any(Wrapper.class))).thenReturn(List.of(relation));
        when(permissionMapper.selectBatchIds(anyCollection()))
                .thenReturn(List.of(buildPermission(1L, "MENU_SKU", "SKU 管理", 0L, "MENU")));
        when(permissionMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(buildPermission(1L, "MENU_SKU", "SKU 管理", 0L, "MENU"),
                        buildPermission(2L, "MENU_ORDER", "订单管理", 0L, "MENU")));

        List<PermissionNodeDTO> menus = roleService.getUserMenus(USER_ID);

        assertThat(menus).hasSize(1);
        assertThat(menus.get(0).getPermissionCode()).isEqualTo("MENU_SKU");
    }

    @Test
    @DisplayName("配置角色权限：先删除原有关联再插入新关联")
    void updateRolePermissions_shouldDeleteAndInsert() {
        SysRole role = new SysRole();
        role.setId(ROLE_ID);
        role.setRoleCode("PICKER");
        when(roleMapper.selectById(ROLE_ID)).thenReturn(role);
        when(permissionMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(buildPermission(1L, "sku:view", "查询", 1L, "BUTTON")));

        roleService.updateRolePermissions(ROLE_ID, List.of("sku:view"));

        verify(rolePermissionMapper).delete(any(Wrapper.class));
        verify(rolePermissionMapper).insert(any(SysRolePermission.class));
    }

    @Test
    @DisplayName("配置角色权限：角色不存在时抛出 60002")
    void updateRolePermissions_shouldThrow_whenRoleNotExists() {
        when(roleMapper.selectById(ROLE_ID)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> roleService.updateRolePermissions(ROLE_ID, List.of("sku:view")));

        assertThat(ex.getCode()).isEqualTo(60002);
    }

    @Test
    @DisplayName("修改角色描述：角色不存在时抛出 60002")
    void updateRole_shouldThrow_whenRoleNotExists() {
        when(roleMapper.selectById(ROLE_ID)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> roleService.updateRole(ROLE_ID, "拣货员", "负责拣货"));

        assertThat(ex.getCode()).isEqualTo(60002);
    }

    @Test
    @DisplayName("查询角色列表")
    void listRoles_shouldReturnRoles() {
        when(roleMapper.selectList(any(Wrapper.class))).thenReturn(List.of(new SysRole()));

        assertThat(roleService.listRoles()).hasSize(1);
    }

    private SysPermission buildPermission(Long id, String code, String name, Long parentId, String type) {
        SysPermission permission = new SysPermission();
        permission.setId(id);
        permission.setPermissionCode(code);
        permission.setPermissionName(name);
        permission.setParentId(parentId);
        permission.setPermissionType(type);
        permission.setSort(1);
        return permission;
    }
}
