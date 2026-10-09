package com.wms.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.PageResult;
import com.wms.system.domain.dto.UserCreateDTO;
import com.wms.system.domain.dto.UserUpdateDTO;
import com.wms.system.domain.entity.SysRole;
import com.wms.system.domain.entity.SysUser;
import com.wms.system.domain.query.UserQuery;
import com.wms.system.domain.vo.UserVO;
import com.wms.system.mapper.SysRoleMapper;
import com.wms.system.mapper.SysUserMapper;
import com.wms.system.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户服务单元测试
 *
 * <p>覆盖测试点：TP-R1-1.2.1-01 ~ TP-R1-1.2.1-13
 *
 * @author WMS
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final String USERNAME = "picker01";

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Spy
    private PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        // 手动构造以显式注入 ServiceImpl 的 baseMapper（Mockito 构造器注入不会填充父类 protected 字段）
        userService = new UserServiceImpl(roleMapper, passwordEncoder, redisTemplate);
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
        ReflectionTestUtils.setField(userService, "defaultPassword", "123456");
    }

    @Test
    @DisplayName("新增用户成功：密码加密且强制修改密码")
    void createUser_shouldEncryptPassword_whenUsernameNotExists() {
        when(userMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        userService.createUser(buildCreateDTO());

        verify(userMapper).insert(any(SysUser.class));
    }

    @Test
    @DisplayName("用户名已存在：抛出 10004")
    void createUser_shouldThrow_whenUsernameExists() {
        when(userMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        BizException ex = assertThrows(BizException.class,
                () -> userService.createUser(buildCreateDTO()));

        assertThat(ex.getCode()).isEqualTo(10004);
    }

    @Test
    @DisplayName("修改用户成功：更新字段并清理权限缓存")
    void updateUser_shouldUpdate_whenUserExists() {
        when(userMapper.selectById(USER_ID)).thenReturn(buildUser());

        userService.updateUser(USER_ID, buildUpdateDTO());

        verify(userMapper).updateById(any(SysUser.class));
        verify(redisTemplate).delete(any(String.class));
    }

    @Test
    @DisplayName("修改不存在的用户：抛出 404")
    void updateUser_shouldThrow_whenUserNotExists() {
        when(userMapper.selectById(USER_ID)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> userService.updateUser(USER_ID, buildUpdateDTO()));

        assertThat(ex.getCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("停用用户：更新状态")
    void changeStatus_shouldUpdateStatus() {
        when(userMapper.selectById(USER_ID)).thenReturn(buildUser());

        userService.changeStatus(USER_ID, 0);

        verify(userMapper).updateById(any(SysUser.class));
    }

    @Test
    @DisplayName("重置密码：返回初始密码并置为强制修改")
    void resetPassword_shouldReturnDefaultPassword() {
        when(userMapper.selectById(USER_ID)).thenReturn(buildUser());

        String password = userService.resetPassword(USER_ID);

        assertThat(password).isEqualTo("123456");
        verify(userMapper).updateById(any(SysUser.class));
    }

    @Test
    @DisplayName("修改密码：原密码错误时抛出 10005")
    void changePassword_shouldThrow_whenOldPasswordWrong() {
        when(userMapper.selectById(USER_ID)).thenReturn(buildUser());

        BizException ex = assertThrows(BizException.class,
                () -> userService.changePassword(USER_ID, "wrong-old", "Abcd1234"));

        assertThat(ex.getCode()).isEqualTo(10005);
    }

    @Test
    @DisplayName("修改密码成功：更新密码并清除强制改密标记")
    void changePassword_shouldUpdate_whenOldPasswordCorrect() {
        when(userMapper.selectById(USER_ID)).thenReturn(buildUser());

        userService.changePassword(USER_ID, "123456", "Abcd1234");

        verify(userMapper).updateById(any(SysUser.class));
    }

    @Test
    @DisplayName("分页查询：补全角色编码与角色名称")
    @SuppressWarnings("unchecked")
    void page_shouldFillRoleInfo() {
        SysUser user = buildUser();
        Page<SysUser> mockPage = new Page<>(1, 20);
        mockPage.setRecords(List.of(user));
        mockPage.setTotal(1);
        when(userMapper.selectPage(any(IPage.class), any(Wrapper.class))).thenReturn(mockPage);
        SysRole role = new SysRole();
        role.setId(1L);
        role.setRoleCode("PICKER");
        role.setRoleName("拣货员");
        when(roleMapper.selectBatchIds(anyCollection())).thenReturn(List.of(role));

        PageResult<UserVO> result = userService.page(new UserQuery());

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getList().get(0).getRoleCode()).isEqualTo("PICKER");
        assertThat(result.getList().get(0).getRoleName()).isEqualTo("拣货员");
    }

    @Test
    @DisplayName("根据用户名查询用户")
    void getByUsername_shouldReturnUser() {
        when(userMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(buildUser());

        SysUser user = userService.getByUsername(USERNAME);

        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo(USERNAME);
    }

    @Test
    @DisplayName("用户名存在性校验")
    void existsUsername_shouldReturnTrue_whenCountGreaterThanZero() {
        when(userMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        assertThat(userService.existsUsername(USERNAME)).isTrue();
    }

    /* ==================== 辅助方法 ==================== */

    private SysUser buildUser() {
        SysUser user = new SysUser();
        user.setId(USER_ID);
        user.setUsername(USERNAME);
        user.setRealName("赵六");
        user.setPassword("{noop}123456");
        user.setRoleId(1L);
        user.setStatus(1);
        return user;
    }

    private UserCreateDTO buildCreateDTO() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername(USERNAME);
        dto.setRealName("赵六");
        dto.setRoleId(1L);
        dto.setPhone("13800138000");
        dto.setStatus(1);
        return dto;
    }

    private UserUpdateDTO buildUpdateDTO() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setRealName("赵六");
        dto.setRoleId(1L);
        dto.setPhone("13800138000");
        return dto;
    }
}
