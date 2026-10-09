package com.wms.base.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SKU 条形码实体（base_sku_barcode）
 *
 * @author WMS
 */
@Data
@TableName("base_sku_barcode")
public class BaseSkuBarcode {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** SKU ID */
    private Long skuId;

    /** 条形码（全局唯一） */
    private String barcode;

    /** 包装规格 */
    private String packageSpec;

    /** 状态：1 启用 0 停用 */
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Integer deleted;
}
