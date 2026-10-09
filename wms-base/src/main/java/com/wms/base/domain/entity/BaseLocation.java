package com.wms.base.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库位实体（base_location）
 *
 * @author WMS
 */
@Data
@TableName("base_location")
public class BaseLocation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 库位编码 */
    private String locationCode;

    /** 仓库 ID */
    private Long warehouseId;

    /** 区域 ID */
    private Long zoneId;

    /** 货架号 */
    private Integer shelfNo;

    /** 层号 */
    private Integer layerNo;

    /** 列号 */
    private Integer columnNo;

    /** 位号 */
    private Integer positionNo;

    /** 库位类型：NORMAL / TEMP / DEFECT / RETURN_PENDING */
    private String locationType;

    /** 状态：FREE / OCCUPIED / LOCKED / MAINTENANCE */
    private String status;

    /** 最大承重（kg） */
    private BigDecimal maxWeight;

    /** 最大体积（m³） */
    private BigDecimal maxVolume;

    /** 已用承重（kg） */
    private BigDecimal usedWeight;

    /** 已用体积（m³） */
    private BigDecimal usedVolume;

    /** 锁定 / 维修原因 */
    private String lockReason;

    /** 状态变更时间 */
    private LocalDateTime statusUpdateTime;

    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    private Integer deleted;
}
