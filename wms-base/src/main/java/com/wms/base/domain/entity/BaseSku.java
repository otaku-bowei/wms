package com.wms.base.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SKU 主数据实体（base_sku）
 *
 * @author WMS
 */
@Data
@TableName("base_sku")
public class BaseSku {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** SKU 编码 */
    private String skuCode;

    /** SPU 编码 */
    private String spuCode;

    /** 商品名称 */
    private String skuName;

    /** 颜色 */
    private String color;

    /** 尺码 */
    private String size;

    /** 类目 ID */
    private Long categoryId;

    /** 商品图片 */
    private String imageUrl;

    /** 重量（克） */
    private BigDecimal weight;

    /** 体积（毫升） */
    private BigDecimal volume;

    /** 长（mm） */
    private Integer lengthMm;

    /** 宽（mm） */
    private Integer widthMm;

    /** 高（mm） */
    private Integer heightMm;

    /** 保质期（天） */
    private Integer shelfLifeDays;

    /** 状态：1 启用 0 停用 */
    private Integer status;

    /** 创建来源：MANUAL / IMPORT */
    private String createdFrom;

    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    private Integer deleted;
}
