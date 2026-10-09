package com.wms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.core.result.PageResult;
import com.wms.common.mybatis.convert.PageConvert;
import com.wms.common.redis.constant.RedisKeys;
import com.wms.system.domain.dto.UserCreateDTO;
import com.wms.system.domain.dto.UserUpdateDTO;
import com.wms.system.domain.entity.SysRole;
import com.wms.system.domain.entity.SysUser;
import com.wms.system.domain.query.UserQuery;
import com.wms.system.domain.vo.UserVO;
import com.wms.system.mapper.SysRoleMapper;
import com.wms.system.mapper.SysUserMapper;
import com.wms.system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * @author WMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @Value("${wms.password.default-password:123456}")
    private String defaultPassword;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateDTO dto) {
        if (existsUsername(dto.getUsername())) {
            throw new BizException(ErrorCode.USERNAME_EXISTS);
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getRealName());
        user.setRoleId(dto.getRoleId());
        user.setWarehouseId(dto.getWarehouseId());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        user.setPassword(passwordEncoder.encode(defaultPassword));
        user.setForceChangePassword(1);
        user.setFailCount(0);
        this.save(user);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserUpdateDTO dto) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND.getCode(), "用户不存在");
        }
        user.setRealName(dto.getRealName());
        user.setRoleId(dto.getRoleId());
        user.setWarehouseId(dto.getWarehouseId());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        this.updateById(user);
        clearPermissionCache(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND.getCode(), "用户不存在");
        }
        user.setStatus(status);
        this.updateById(user);
        clearPermissionCache(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String resetPassword(Long id) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND.getCode(), "用户不存在");
        }
        user.setPassword(passwordEncoder.encode(defaultPassword));
        user.setForceChangePassword(1);
        user.setFailCount(0);
        user.setLockTime(null);
        this.updateById(user);
        return defaultPassword;
    }

    @Override
    public void updateLoginInfo(Long id, String ipAddress) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        user.setFailCount(0);
        this.updateById(user);
    }

    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND.getCode(), "用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BizException(ErrorCode.OLD_PASSWORD_ERROR);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setForceChangePassword(0);
        this.updateById(user);
        clearPermissionCache(id);
    }

    @Override
    public UserVO getById(Long id) {
        SysUser user = this.getById(id);
        if (user == null) {
            return null;
        }
        return toVO(user, Map.of());
    }

    @Override
    public PageResult<UserVO> page(UserQuery query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(StringUtils.hasText(query.getKeyword()), SysUser::getUsername, query.getKeyword())
                .or().like(StringUtils.hasText(query.getKeyword()), SysUser::getRealName, query.getKeyword())
                .eq(query.getRoleId() != null, SysUser::getRoleId, query.getRoleId())
                .eq(query.getWarehouseId() != null, SysUser::getWarehouseId, query.getWarehouseId())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .orderByDesc(SysUser::getId);
        IPage<SysUser> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        Map<Long, SysRole> roleMap = loadRoleMap(page.getRecords());
        List<UserVO> voList = page.getRecords().stream()
                .map(user -> toVO(user, roleMap))
                .toList();
        return PageResult.of(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public SysUser getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    @Override
    public boolean existsUsername(String username) {
        return this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)) > 0;
    }

    /* ==================== 私有方法 ==================== */

    private Map<Long, SysRole> loadRoleMap(List<SysUser> users) {
        List<Long> roleIds = users.stream().map(SysUser::getRoleId).filter(id -> id != null).distinct().toList();
        if (roleIds.isEmpty()) {
            return Map.of();
        }
        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
        return roles.stream().collect(Collectors.toMap(SysRole::getId, Function.identity(), (a, b) -> a));
    }

    private UserVO toVO(SysUser user, Map<Long, SysRole> roleMap) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        SysRole role = roleMap.get(user.getRoleId());
        if (role != null) {
            vo.setRoleCode(role.getRoleCode());
            vo.setRoleName(role.getRoleName());
        }
        return vo;
    }

    private void clearPermissionCache(Long userId) {
        try {
            redisTemplate.delete(RedisKeys.userPerms(userId));
        } catch (Exception e) {
            log.warn("清理权限缓存失败：userId={}", userId);
        }
    }
}
