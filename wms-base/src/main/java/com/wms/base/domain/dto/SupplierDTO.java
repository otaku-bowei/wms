package com.wms.base.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 供应商新增 / 修改参数
 *
 * @author WMS
 */
@Data
public class SupplierDTO {

    /** 供应商编码 */
    @NotBlank(message = "供应商编码不能为空")
    private String supplierCode;

    /** 供应商名称 */
    @NotBlank(message = "供应商名称不能为空")
    private String supplierName;

    /** 联系人 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 地址 */
    private String address;

    /** 备注 */
    private String remark;

    /** 状态：1 启用 0 停用 */
    private Integer status = 1;
}
