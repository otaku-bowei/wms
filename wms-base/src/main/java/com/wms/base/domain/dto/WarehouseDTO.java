package com.wms.base.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 仓库新增 / 修改参数
 *
 * @author WMS
 */
@Data
public class WarehouseDTO {

    /** 仓库编码（新增必填，修改时不可变更） */
    private String warehouseCode;

    /** 仓库名称 */
    @NotBlank(message = "仓库名称不能为空")
    private String warehouseName;

    /** 仓库类型：CENTRAL / REGIONAL / OVERSEAS / LOCAL */
    @NotBlank(message = "仓库类型不能为空")
    private String warehouseType;

    /** 所属区域 */
    private String region;

    /** 地址 */
    private String address;

    /** 负责人 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 备注 */
    private String remark;

    /** 状态：1 启用 0 停用 */
    private Integer status = 1;
}
