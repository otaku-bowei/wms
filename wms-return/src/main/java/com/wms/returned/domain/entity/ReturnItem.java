package com.wms.returned.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 退货明细实体（质检分级 + 重新上架）。
 */
@Data
@TableName("return_item")
public class ReturnItem implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 退货单 ID */
    private Long returnId;

    /** SKU ID */
    private Long skuId;

    /** 建议上架库位 */
    private Long locationId;

    /** 退货数量 */
    private Integer planQty;

    /** 实收数量 */
    private Integer receivedQty;

    /** 质检等级 A/B/C */
    private String grade;

    /** 已重新上架数量 */
    private Integer reshelvedQty;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
