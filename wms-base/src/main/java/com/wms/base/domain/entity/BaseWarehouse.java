package com.wms.base.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仓库实体（base_warehouse）
 *
 * @author WMS
 */
@Data
@TableName("base_warehouse")
public class BaseWarehouse {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 仓库编码 */
    private String warehouseCode;

    /** 仓库名称 */
    private String warehouseName;

    /** 仓库类型：CENTRAL / REGIONAL / OVERSEAS / LOCAL */
    private String warehouseType;

    /** 所属区域 */
    private String region;

    /** 地址 */
    private String address;

    /** 负责人 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 状态：1 启用 0 停用 */
    private Integer status;

    /** 备注 */
    private String remark;

    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    private Integer deleted;
}
