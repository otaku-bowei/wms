package com.wms.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 修改用户参数
 *
 * @author WMS
 */
@Data
public class UserUpdateDTO {

    /** 姓名 */
    @NotBlank(message = "姓名不能为空")
    private String realName;

    /** 角色 ID */
    private Long roleId;

    /** 所属仓库 ID */
    private Long warehouseId;

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 邮箱 */
    private String email;
}
