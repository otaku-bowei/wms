package com.wms.system.service;

import com.wms.common.core.result.PageResult;
import com.wms.system.domain.dto.UserCreateDTO;
import com.wms.system.domain.dto.UserUpdateDTO;
import com.wms.system.domain.entity.SysUser;
import com.wms.system.domain.query.UserQuery;
import com.wms.system.domain.vo.UserVO;

/**
 * 用户服务接口
 *
 * @author WMS
 */
public interface UserService {

    /**
     * 新增用户
     *
     * @param dto 参数
     * @return 用户 ID
     */
    Long createUser(UserCreateDTO dto);

    /**
     * 修改用户
     *
     * @param id  用户 ID
     * @param dto 参数
     */
    void updateUser(Long id, UserUpdateDTO dto);

    /**
     * 启用 / 停用用户
     *
     * @param id     用户 ID
     * @param status 1 启用 0 停用
     */
    void changeStatus(Long id, Integer status);

    /**
     * 重置密码为初始密码
     *
     * @param id 用户 ID
     * @return 重置后的明文密码
     */
    String resetPassword(Long id);

    /**
     * 更新最后登录信息
     *
     * @param id        用户 ID
     * @param ipAddress 登录 IP
     */
    void updateLoginInfo(Long id, String ipAddress);

    /**
     * 修改密码
     *
     * @param id           用户 ID
     * @param oldPassword  原密码
     * @param newPassword  新密码
     */
    void changePassword(Long id, String oldPassword, String newPassword);

    /**
     * 查询用户详情
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    UserVO getById(Long id);

    /**
     * 分页查询用户
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<UserVO> page(UserQuery query);

    /**
     * 根据用户名查询用户（含密码）
     *
     * @param username 用户名
     * @return 用户实体
     */
    SysUser getByUsername(String username);

    /**
     * 用户名是否已存在
     *
     * @param username 用户名
     * @return true 已存在
     */
    boolean existsUsername(String username);
}
