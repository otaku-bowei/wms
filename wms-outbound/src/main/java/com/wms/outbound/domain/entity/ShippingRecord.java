package com.wms.outbound.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 装车交接记录实体。
 */
@Data
@TableName("shipping_record")
public class ShippingRecord implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 出库单 ID */
    private Long outboundId;

    /** 包裹 ID */
    private Long packageId;

    /** 物流商 ID */
    private Long carrierId;

    /** 运单号 */
    private String trackingNo;

    /** 车辆号 */
    private String vehicleNo;

    /** LOADED/DELIVERED */
    private String status;

    /** 装车人 */
    private String loader;

    /** 装车时间 */
    private LocalDateTime loadTime;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
