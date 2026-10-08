package com.wms.api.system.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 权限菜单树节点（跨服务传输）
 *
 * @author WMS
 */
@Data
public class PermissionNodeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 权限 ID */
    private Long id;

    /** 权限标识 */
    private String permissionCode;

    /** 权限名称 */
    private String permissionName;

    /** 父级 ID */
    private Long parentId;

    /** 类型：MENU / BUTTON */
    private String permissionType;

    /** 路由路径 */
    private String path;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sort;

    /** 子节点 */
    private List<PermissionNodeDTO> children;
}
