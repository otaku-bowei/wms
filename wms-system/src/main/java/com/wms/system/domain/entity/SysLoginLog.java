package com.wms.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体（sys_login_log）
 *
 * @author WMS
 */
@Data
@TableName("sys_login_log")
public class SysLoginLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;

    /** IP 地址 */
    private String ipAddress;

    /** 登录地点 */
    private String location;

    /** 浏览器 */
    private String browser;

    /** 操作系统 */
    private String os;

    /** 登录结果：SUCCESS / FAIL / LOCKED */
    private String loginResult;

    /** 失败原因 */
    private String failReason;

    /** 登录时间 */
    private LocalDateTime loginTime;
}
