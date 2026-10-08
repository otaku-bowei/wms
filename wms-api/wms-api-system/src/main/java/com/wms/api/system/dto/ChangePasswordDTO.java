package com.wms.api.system.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 修改密码参数（跨服务传输）
 *
 * @author WMS
 */
@Data
public class ChangePasswordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 原密码 */
    private String oldPassword;

    /** 新密码 */
    private String newPassword;
}
