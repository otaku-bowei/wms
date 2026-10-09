package com.wms.base.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 区域新增 / 修改参数
 *
 * @author WMS
 */
@Data
public class ZoneDTO {

    /** 区域编码 */
    @NotBlank(message = "区域编码不能为空")
    private String zoneCode;

    /** 区域名称 */
    @NotBlank(message = "区域名称不能为空")
    private String zoneName;

    /** 区域类型：STORAGE / RECEIVING / SHIPPING / QC / RETURN */
    @NotBlank(message = "区域类型不能为空")
    private String zoneType;

    /** 周转分区：FAST / SLOW / DEAD */
    private String turnoverZone;

    /** 状态：1 启用 0 停用 */
    private Integer status = 1;
}
