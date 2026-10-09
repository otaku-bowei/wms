package com.wms.base.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 供应商实体（base_supplier）
 *
 * @author WMS
 */
@Data
@TableName("base_supplier")
public class BaseSupplier {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 供应商编码 */
    private String supplierCode;

    /** 供应商名称 */
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
    private Integer status;

    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    private Integer deleted;
}
