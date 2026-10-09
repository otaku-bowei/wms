package com.wms.base.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品类目实体（base_category）
 *
 * @author WMS
 */
@Data
@TableName("base_category")
public class BaseCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 类目编码 */
    private String categoryCode;

    /** 类目名称 */
    private String categoryName;

    /** 父级 ID */
    private Long parentId;

    /** 层级 */
    private Integer level;

    /** 层级路径 */
    private String path;

    /** 排序 */
    private Integer sort;

    /** 状态 */
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Integer deleted;
}
