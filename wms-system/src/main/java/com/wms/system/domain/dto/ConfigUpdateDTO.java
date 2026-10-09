package com.wms.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 系统参数修改项
 *
 * @author WMS
 */
@Data
public class ConfigUpdateDTO {

    /** 参数键 */
    @NotBlank(message = "参数键不能为空")
    private String paramKey;

    /** 参数值 */
    private String paramValue;
}
