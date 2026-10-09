package com.wms.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统权限实体（sys_permission）
 *
 * @author WMS
 */
@Data
@TableName("sys_permission")
public class SysPermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 权限标识，如 sku:create */
    private String permissionCode;

    /** 权限名称 */
    private String permissionName;

    /** 父级 ID，0 为顶级 */
    private Long parentId;

    /** 类型：MENU 菜单 / BUTTON 按钮 */
    private String permissionType;

    /** 路由路径 */
    private String path;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sort;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Integer deleted;
}
