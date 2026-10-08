package com.wms.common.log.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作类型
 *
 * @author WMS
 */
@Getter
@AllArgsConstructor
public enum OperationType {

    CREATE("CREATE", "新增"),
    UPDATE("UPDATE", "修改"),
    DELETE("DELETE", "删除"),
    LOGIN("LOGIN", "登录"),
    LOGOUT("LOGOUT", "退出"),
    ENABLE("ENABLE", "启用"),
    DISABLE("DISABLE", "停用"),
    LOCK("LOCK", "锁定"),
    IMPORT("IMPORT", "导入"),
    EXPORT("EXPORT", "导出"),
    APPROVE("APPROVE", "审批"),
    RESET("RESET", "重置"),
    OTHER("OTHER", "其他"),
    ;

    private final String code;
    private final String name;
}
