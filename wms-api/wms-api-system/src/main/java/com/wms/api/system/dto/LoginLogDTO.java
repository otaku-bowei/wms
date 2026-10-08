package com.wms.api.system.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录日志写入参数（跨服务传输）
 *
 * @author WMS
 */
@Data
public class LoginLogDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户名 */
    private String username;

    /** IP 地址 */
    private String ipAddress;

    /** 浏览器 */
    private String browser;

    /** 操作系统 */
    private String os;

    /** 登录结果：SUCCESS / FAIL / LOCKED */
    private String loginResult;

    /** 失败原因 */
    private String failReason;
}
