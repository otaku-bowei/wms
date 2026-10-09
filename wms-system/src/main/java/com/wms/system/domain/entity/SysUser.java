package com.wms.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体（sys_user）
 *
 * @author WMS
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** 姓名 */
    private String realName;

    /** 密码（BCrypt 密文） */
    private String password;

    /** 角色 ID */
    private Long roleId;

    /** 所属仓库 ID */
    private Long warehouseId;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 状态：1 启用 0 停用 */
    private Integer status;

    /** 连续登录失败次数 */
    private Integer failCount;

    /** 账户锁定时间 */
    private LocalDateTime lockTime;

    /** 是否强制修改密码：1 是 0 否 */
    private Integer forceChangePassword;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 最后登录 IP */
    private String lastLoginIp;

    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    /** 逻辑删除：0 否 1 是 */
    private Integer deleted;
}
