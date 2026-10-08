package com.wms.auth.service;

import com.wms.auth.domain.dto.ChangePasswordRequest;
import com.wms.auth.domain.dto.LoginDTO;
import com.wms.auth.domain.vo.LoginVO;
import com.wms.auth.domain.vo.TokenVO;
import com.wms.auth.domain.vo.UserInfoVO;

/**
 * 认证服务接口
 *
 * @author WMS
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param dto      登录参数
     * @param ipAddress 登录 IP
     * @return 登录结果
     */
    LoginVO login(LoginDTO dto, String ipAddress);

    /**
     * 退出登录（Token 加入黑名单）
     *
     * @param token JWT Token
     */
    void logout(String token);

    /**
     * 修改密码
     *
     * @param userId  用户 ID
     * @param request 修改密码参数
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * 查询当前用户信息
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    UserInfoVO currentUser(Long userId);

    /**
     * 查询当前用户菜单与权限
     *
     * @param userId 用户 ID
     * @return 菜单树与权限集合
     */
    TokenVO menus(Long userId);

    /**
     * 刷新 Token
     *
     * @param token 旧 Token
     * @return 新 Token
     */
    TokenVO refresh(String token);
}
