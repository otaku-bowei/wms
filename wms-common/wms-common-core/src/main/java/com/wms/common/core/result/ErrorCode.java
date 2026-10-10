package com.wms.common.core.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 *
 * <p>通用错误码（0/4xx/5xx）与业务错误码（1xxxx-5xxxx）统一定义，
 * 与接口文档《1.5 错误码》章节保持一致。
 *
 * @author WMS
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    /* ==================== 通用错误码 ==================== */
    SUCCESS(0, "success"),
    PARAM_ERROR(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证或 Token 已失效"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    SYSTEM_ERROR(500, "系统内部错误"),

    /* ==================== 用户与认证 1xxxx ==================== */
    USER_CREDENTIAL_ERROR(10001, "用户名或密码错误"),
    USER_DISABLED(10002, "账户已停用，请联系管理员"),
    USER_LOCKED(10003, "账户已锁定，请 30 分钟后重试"),
    USERNAME_EXISTS(10004, "用户名已存在"),
    OLD_PASSWORD_ERROR(10005, "原密码错误"),
    PASSWORD_TOO_SHORT(10006, "密码长度不能少于 8 位"),
    PASSWORD_NEED_LETTER_NUMBER(10007, "密码需包含字母和数字"),

    /* ==================== SKU 2xxxx ==================== */
    SKU_CODE_EXISTS(20001, "SKU 编码已存在，请重新输入"),
    SKU_BARCODE_OCCUPIED(20002, "该条形码已被其他 SKU 占用"),
    SKU_HAS_STOCK(20003, "该 SKU 存在库存，无法删除，请先停用"),
    SKU_REQUIRED_FIELD_EMPTY(20004, "必填字段不能为空"),
    SKU_IMPORT_TEMPLATE_ERROR(20005, "SKU 导入模板格式错误"),
    SKU_NOT_FOUND(20006, "SKU 不存在"),
    SKU_DELETE_FORBIDDEN(20007, "该 SKU 已产生业务记录，不可删除"),

    /* ==================== 入库 21xxxx ==================== */
    INBOUND_NO_EXISTS(21001, "入库单号已存在"),
    INBOUND_NOT_FOUND(21002, "入库单不存在"),
    INBOUND_STATUS_INVALID(21003, "入库单状态不允许该操作"),
    INBOUND_QTY_EXCEED(21004, "上架数量超过待上架数量"),

    /* ==================== 库存 22xxxx ==================== */
    STOCK_NOT_FOUND(22001, "库存记录不存在"),
    STOCK_NOT_ENOUGH(22002, "可用库存不足"),

    /* ==================== 调整 23xxxx ==================== */
    ADJUST_NOT_FOUND(23001, "调整单不存在"),
    ADJUST_STATUS_INVALID(23002, "调整单状态不允许审批"),

    /* ==================== 仓库 3xxxx ==================== */
    WAREHOUSE_CODE_EXISTS(30001, "仓库编码已存在"),
    WAREHOUSE_DISABLED(30002, "仓库已停用，无法创建出入库单"),
    ZONE_CODE_EXISTS(30003, "区域编码已存在"),
    WAREHOUSE_NOT_FOUND(30004, "仓库不存在"),
    ZONE_NOT_FOUND(30005, "区域不存在"),

    /* ==================== 库位 4xxxx ==================== */
    LOCATION_CODE_EXISTS(40001, "库位编码已存在"),
    LOCATION_OCCUPIED(40002, "库位已被占用"),
    LOCATION_UNAVAILABLE(40003, "库位处于锁定/维修中，不可用于上架"),
    LOCATION_BATCH_LIMIT(40004, "批量创建数量超出限制（单次 ≤ 5000）"),
    LOCATION_STATUS_INVALID(40005, "库位状态不允许该操作"),
    LOCATION_NOT_FOUND(40006, "库位不存在"),

    /* ==================== 供应商 5xxxx ==================== */
    SUPPLIER_CODE_EXISTS(50001, "供应商编码已存在"),
    SUPPLIER_DISABLED(50002, "供应商已停用"),
    SUPPLIER_NOT_FOUND(50003, "供应商不存在"),

    /* ==================== 角色与权限 6xxxx ==================== */
    ROLE_BUILTIN_NOT_DELETABLE(60001, "系统预置角色不可删除"),
    ROLE_NOT_FOUND(60002, "角色不存在"),
    ROLE_CODE_EXISTS(60003, "角色编码已存在"),

    /* ==================== 配置 7xxxx ==================== */
    CODE_RULE_NOT_FOUND(70001, "编码规则不存在"),
    DICT_NOT_FOUND(70002, "字典类型不存在"),
    CONFIG_PARAM_NOT_FOUND(70003, "系统参数不存在"),
    ;

    private final int code;
    private final String message;
}
