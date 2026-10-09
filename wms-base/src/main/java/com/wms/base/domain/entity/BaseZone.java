package com.wms.base.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仓库区域实体（base_zone）
 *
 * @author WMS
 */
@Data
@TableName("base_zone")
public class BaseZone {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 仓库 ID */
    private Long warehouseId;

    /** 区域编码 */
    private String zoneCode;

    /** 区域名称 */
    private String zoneName;

    /** 区域类型：STORAGE / RECEIVING / SHIPPING / QC / RETURN */
    private String zoneType;

    /** 周转分区：FAST / SLOW / DEAD */
    private String turnoverZone;

    /** 状态：1 启用 0 停用 */
    private Integer status;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Integer deleted;
}
