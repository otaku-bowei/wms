package com.wms.outbound.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出库包裹（复核打包）实体。
 */
@Data
@TableName("outbound_package")
public class OutboundPackage implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 包裹号 */
    private String packageNo;

    /** 出库单 ID */
    private Long outboundId;

    /** 物流商 ID */
    private Long carrierId;

    /** 运单号 */
    private String trackingNo;

    /** 包裹重量(kg) */
    private BigDecimal weight;

    /** PENDING/PACKED/SHIPPED */
    private String status;

    /** 打包人 */
    private String packer;

    /** 打包时间 */
    private LocalDateTime packTime;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
