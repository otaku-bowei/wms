package com.wms.base.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SKU 视图对象
 *
 * @author WMS
 */
@Data
public class SkuVO {

    private Long id;
    private String skuCode;
    private String spuCode;
    private String skuName;
    private String color;
    private String size;
    private Long categoryId;

    /** 类目名称 */
    private String categoryName;

    private String imageUrl;
    private BigDecimal weight;
    private BigDecimal volume;
    private Integer lengthMm;
    private Integer widthMm;
    private Integer heightMm;
    private Integer shelfLifeDays;
    private Integer status;

    /** 条形码列表（一品多码） */
    private List<String> barcodes;

    /** 库存汇总（R2 起生效，当前为 0） */
    private Integer totalQty;
    private Integer availableQty;
    private Integer lockedQty;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
