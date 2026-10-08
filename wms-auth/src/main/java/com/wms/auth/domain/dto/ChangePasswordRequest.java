package com.wms.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码请求参数
 *
 * @author WMS
 */
@Data
public class ChangePasswordRequest {

    /** 原密码 */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /** 新密码 */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    /** 确认新密码 */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
