package com.wms.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 新增用户参数
 *
 * @author WMS
 */
@Data
public class UserCreateDTO {

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名为 4-20 位字母、数字或下划线")
    private String username;

    /** 姓名 */
    @NotBlank(message = "姓名不能为空")
    private String realName;

    /** 角色 ID */
    @NotNull(message = "角色不能为空")
    private Long roleId;

    /** 所属仓库 ID */
    private Long warehouseId;

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 邮箱 */
    private String email;

    /** 状态：1 启用 0 停用 */
    private Integer status = 1;
}
