-- ===================================================================
-- WMS 电商仓储管理系统
-- 版本：R1（基础平台与主数据）
-- 脚本：R1-V1.0.0-schema.sql  建表脚本（DDL）
-- 数据库：MySQL 8.0+
-- 说明：创建系统域（sys_）10 张表 + 基础数据域（base_）7 张表，共 17 张
-- 依赖文档：需求分析/数据库设计/R1-基础平台与主数据数据库设计.md
-- ===================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `wms`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE `wms`;

-- ===================================================================
-- 一、系统域（sys_）
-- ===================================================================

-- -------------------------------------------------------------------
-- 1. sys_role 角色表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code`     VARCHAR(50)  NOT NULL COMMENT '角色编码（唯一）',
  `role_name`     VARCHAR(50)  NOT NULL COMMENT '角色名称',
  `description`   VARCHAR(200)     DEFAULT NULL COMMENT '角色描述',
  `builtin`       TINYINT      NOT NULL DEFAULT 1 COMMENT '是否系统内置：1是（不可删除） 0否',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `create_by`     VARCHAR(50)      DEFAULT NULL COMMENT '创建人',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`     VARCHAR(50)      DEFAULT NULL COMMENT '修改人',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- -------------------------------------------------------------------
-- 2. sys_permission 权限表（菜单 + 按钮）
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限标识（唯一），如 sku:create',
  `permission_name` VARCHAR(50)  NOT NULL COMMENT '权限名称',
  `parent_id`       BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级ID，0为顶级',
  `permission_type` VARCHAR(20)  NOT NULL DEFAULT 'BUTTON' COMMENT '类型：MENU菜单 BUTTON按钮',
  `path`            VARCHAR(200)     DEFAULT NULL COMMENT '前端路由路径（菜单用）',
  `icon`            VARCHAR(50)      DEFAULT NULL COMMENT '菜单图标',
  `sort`            INT          NOT NULL DEFAULT 0 COMMENT '排序号',
  `remark`          VARCHAR(200)     DEFAULT NULL COMMENT '备注',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限表（菜单/按钮）';

-- -------------------------------------------------------------------
-- 3. sys_user 系统用户表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id`                     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username`               VARCHAR(50)  NOT NULL COMMENT '用户名（唯一）',
  `real_name`              VARCHAR(50)  NOT NULL COMMENT '姓名',
  `password`               VARCHAR(100) NOT NULL COMMENT '密码（BCrypt密文存储）',
  `role_id`                BIGINT UNSIGNED NOT NULL COMMENT '角色ID，关联 sys_role.id',
  `warehouse_id`           BIGINT UNSIGNED     DEFAULT NULL COMMENT '所属仓库ID，关联 base_warehouse.id',
  `phone`                  VARCHAR(20)  NOT NULL COMMENT '手机号',
  `email`                  VARCHAR(100)     DEFAULT NULL COMMENT '邮箱',
  `status`                 TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `fail_count`             INT          NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
  `lock_time`              DATETIME         DEFAULT NULL COMMENT '账户锁定时间',
  `force_change_password`  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否强制修改密码：1是 0否',
  `last_login_time`        DATETIME         DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip`          VARCHAR(50)      DEFAULT NULL COMMENT '最后登录IP',
  `create_by`              VARCHAR(50)      DEFAULT NULL COMMENT '创建人',
  `create_time`            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`              VARCHAR(50)      DEFAULT NULL COMMENT '修改人',
  `update_time`            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted`                TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_warehouse_id` (`warehouse_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- -------------------------------------------------------------------
-- 4. sys_role_permission 角色权限关联表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id`       BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- -------------------------------------------------------------------
-- 5. sys_operation_log 操作日志表（大表，建议按月归档）
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`         BIGINT UNSIGNED     DEFAULT NULL COMMENT '操作人ID',
  `username`        VARCHAR(50)      DEFAULT NULL COMMENT '操作人用户名',
  `module`          VARCHAR(50)      DEFAULT NULL COMMENT '操作模块：SKU/WAREHOUSE/LOCATION/USER/ROLE/CONFIG/AUTH',
  `operation_type`  VARCHAR(20)      DEFAULT NULL COMMENT '操作类型：CREATE UPDATE DELETE LOGIN LOGOUT IMPORT EXPORT APPROVE LOCK',
  `description`     VARCHAR(500)     DEFAULT NULL COMMENT '操作描述',
  `request_params`  TEXT COMMENT              '请求参数JSON（敏感字段脱敏）',
  `ip_address`      VARCHAR(50)      DEFAULT NULL COMMENT 'IP地址',
  `success`         TINYINT      NOT NULL DEFAULT 1 COMMENT '是否成功：1是 0否',
  `error_msg`       VARCHAR(1000)     DEFAULT NULL COMMENT '错误信息',
  `operate_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_module` (`module`),
  KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';

-- -------------------------------------------------------------------
-- 6. sys_login_log 登录日志表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_login_log` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username`      VARCHAR(50)      DEFAULT NULL COMMENT '用户名',
  `ip_address`    VARCHAR(50)      DEFAULT NULL COMMENT 'IP地址',
  `location`      VARCHAR(100)     DEFAULT NULL COMMENT '登录地点（IP解析）',
  `browser`       VARCHAR(100)     DEFAULT NULL COMMENT '浏览器',
  `os`            VARCHAR(100)     DEFAULT NULL COMMENT '操作系统',
  `login_result`  VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS' COMMENT '登录结果：SUCCESS成功 FAIL失败 LOCKED锁定',
  `fail_reason`   VARCHAR(200)     DEFAULT NULL COMMENT '失败原因：密码错误/账户锁定/账户停用/用户名不存在',
  `login_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_login_time` (`login_time`),
  KEY `idx_login_result` (`login_result`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统登录日志表';

-- -------------------------------------------------------------------
-- 7. sys_config 系统参数表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_config` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `param_key`     VARCHAR(100) NOT NULL COMMENT '参数键（唯一）',
  `param_name`    VARCHAR(100) NOT NULL COMMENT '参数名称',
  `param_value`   VARCHAR(500) NOT NULL COMMENT '参数值',
  `default_value` VARCHAR(500)     DEFAULT NULL COMMENT '默认值',
  `param_group`   VARCHAR(30)  NOT NULL DEFAULT 'SYSTEM' COMMENT '分组：SECURITY安全 BUSINESS业务 SYSTEM系统',
  `need_restart`  TINYINT      NOT NULL DEFAULT 0 COMMENT '修改后是否需重启：1是 0否',
  `remark`        VARCHAR(200)     DEFAULT NULL COMMENT '说明',
  `update_by`     VARCHAR(50)      DEFAULT NULL COMMENT '修改人',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_param_key` (`param_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数配置表';

-- -------------------------------------------------------------------
-- 8. sys_dict 字典类型表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_dict` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_type`   VARCHAR(50)  NOT NULL COMMENT '字典类型（唯一）',
  `dict_name`   VARCHAR(100) NOT NULL COMMENT '字典名称',
  `description` VARCHAR(200)     DEFAULT NULL COMMENT '描述',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典类型表';

-- -------------------------------------------------------------------
-- 9. sys_dict_item 字典项表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_dict_item` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_type`   VARCHAR(50)  NOT NULL COMMENT '字典类型',
  `item_value`  VARCHAR(50)  NOT NULL COMMENT '枚举值，如 CENTRAL',
  `item_label`  VARCHAR(100) NOT NULL COMMENT '枚举名称，如 中心仓',
  `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序号',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用（停用后不在下拉展示） 0停用',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_item` (`dict_type`, `item_value`),
  KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典项表';

-- -------------------------------------------------------------------
-- 10. sys_code_rule 编码规则表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_code_rule` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_type`      VARCHAR(30) NOT NULL COMMENT '单据类型（唯一）：SKU/INBOUND/OUTBOUND/WAVE/STOCKTAKE/ADJUST/TRANSFER/RETURN',
  `rule_name`      VARCHAR(50) NOT NULL COMMENT '规则名称',
  `prefix`         VARCHAR(20)     DEFAULT NULL COMMENT '前缀',
  `date_format`    VARCHAR(20)     DEFAULT NULL COMMENT '日期格式，如 yyyyMMdd；为空表示不含日期',
  `serial_length`  INT         NOT NULL DEFAULT 3 COMMENT '流水号位数',
  `serial_reset`   VARCHAR(20) NOT NULL DEFAULT 'DAY' COMMENT '重置方式：DAY MONTH YEAR NEVER',
  `current_seq`    INT         NOT NULL DEFAULT 0 COMMENT '当前流水号（持久化兜底）',
  `current_date`   VARCHAR(20)     DEFAULT NULL COMMENT '当前日期标识，用于按天/月/年重置判断',
  `sample`         VARCHAR(50)     DEFAULT NULL COMMENT '编码示例',
  `status`         TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `create_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_type` (`rule_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单据编码规则表';


-- ===================================================================
-- 二、基础数据域（base_）
-- ===================================================================

-- -------------------------------------------------------------------
-- 11. base_category 商品类目表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `base_category` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_code` VARCHAR(50)  NOT NULL COMMENT '类目编码（唯一）',
  `category_name` VARCHAR(100) NOT NULL COMMENT '类目名称',
  `parent_id`     BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级ID，0为顶级',
  `level`         INT          NOT NULL DEFAULT 1 COMMENT '层级',
  `path`          VARCHAR(200)     DEFAULT NULL COMMENT '层级路径，如 0/1/10',
  `sort`          INT          NOT NULL DEFAULT 0 COMMENT '排序号',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`category_code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品类目表';

-- -------------------------------------------------------------------
-- 12. base_sku SKU 主表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `base_sku` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sku_code`         VARCHAR(64)      NOT NULL COMMENT 'SKU编码（唯一）',
  `spu_code`         VARCHAR(64)      NOT NULL COMMENT 'SPU编码',
  `sku_name`         VARCHAR(200)     NOT NULL COMMENT '商品名称',
  `color`            VARCHAR(50)      NOT NULL COMMENT '颜色',
  `size`             VARCHAR(50)      NOT NULL COMMENT '尺码',
  `category_id`      BIGINT UNSIGNED      DEFAULT NULL COMMENT '类目ID，关联 base_category.id',
  `image_url`        VARCHAR(500)         DEFAULT NULL COMMENT '商品图片URL',
  `weight`           DECIMAL(10,2)        DEFAULT NULL COMMENT '重量（克）',
  `volume`           DECIMAL(10,2)        DEFAULT NULL COMMENT '体积（毫升）',
  `length_mm`        INT                  DEFAULT NULL COMMENT '长（mm）',
  `width_mm`         INT                  DEFAULT NULL COMMENT '宽（mm）',
  `height_mm`        INT                  DEFAULT NULL COMMENT '高（mm）',
  `shelf_life_days`  INT                  DEFAULT NULL COMMENT '保质期（天）',
  `status`           TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `created_from`     VARCHAR(20)     NOT NULL DEFAULT 'MANUAL' COMMENT '创建来源：MANUAL手工 IMPORT导入',
  `create_by`        VARCHAR(50)          DEFAULT NULL COMMENT '创建人',
  `create_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`        VARCHAR(50)          DEFAULT NULL COMMENT '修改人',
  `update_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted`          TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_code` (`sku_code`),
  UNIQUE KEY `uk_spu_color_size` (`spu_code`, `color`, `size`, `deleted`),
  KEY `idx_spu_code` (`spu_code`),
  KEY `idx_sku_name` (`sku_name`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU主数据表';

-- -------------------------------------------------------------------
-- 13. base_sku_barcode SKU 条形码表（支持一品多码）
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `base_sku_barcode` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sku_id`        BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID，关联 base_sku.id',
  `barcode`       VARCHAR(64)     NOT NULL COMMENT '条形码（全局唯一）',
  `package_spec`  VARCHAR(100)        DEFAULT NULL COMMENT '包装规格，如 单件装/3件装',
  `status`        TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted`       TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_barcode` (`barcode`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU条形码表（一品多码）';

-- -------------------------------------------------------------------
-- 14. base_warehouse 仓库表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `base_warehouse` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_code`  VARCHAR(32)     NOT NULL COMMENT '仓库编码（唯一）',
  `warehouse_name`  VARCHAR(100)    NOT NULL COMMENT '仓库名称',
  `warehouse_type`  VARCHAR(20)     NOT NULL COMMENT '仓库类型：CENTRAL中心仓 REGIONAL区域仓 OVERSEAS海外仓 LOCAL本地仓',
  `region`          VARCHAR(50)         DEFAULT NULL COMMENT '所属区域，如 华南/华东/北美',
  `address`         VARCHAR(200)        DEFAULT NULL COMMENT '地址',
  `contact_name`    VARCHAR(50)         DEFAULT NULL COMMENT '负责人',
  `contact_phone`   VARCHAR(20)         DEFAULT NULL COMMENT '联系电话',
  `status`          TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `remark`          VARCHAR(500)        DEFAULT NULL COMMENT '备注',
  `create_by`       VARCHAR(50)         DEFAULT NULL COMMENT '创建人',
  `create_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`       VARCHAR(50)         DEFAULT NULL COMMENT '修改人',
  `update_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted`         TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_code` (`warehouse_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库主数据表';

-- -------------------------------------------------------------------
-- 15. base_zone 仓库区域表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `base_zone` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_id`  BIGINT UNSIGNED NOT NULL COMMENT '仓库ID，关联 base_warehouse.id',
  `zone_code`     VARCHAR(20)     NOT NULL COMMENT '区域编码，如 A',
  `zone_name`     VARCHAR(50)     NOT NULL COMMENT '区域名称',
  `zone_type`     VARCHAR(20)     NOT NULL COMMENT '区域类型：STORAGE存储区 RECEIVING收货区 SHIPPING发货区 QC质检区 RETURN退货区',
  `turnover_zone` VARCHAR(20)         DEFAULT NULL COMMENT '周转分区：FAST快销区 SLOW慢销区 DEAD滞销区',
  `status`        TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `remark`        VARCHAR(500)        DEFAULT NULL COMMENT '备注',
  `create_time`   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted`       TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_zone` (`warehouse_id`, `zone_code`, `deleted`),
  KEY `idx_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库区域表';

-- -------------------------------------------------------------------
-- 16. base_location 库位表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `base_location` (
  `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `location_code`      VARCHAR(64)     NOT NULL COMMENT '库位编码（唯一），如 WH-001-A-03-02-05',
  `warehouse_id`       BIGINT UNSIGNED NOT NULL COMMENT '仓库ID，关联 base_warehouse.id',
  `zone_id`            BIGINT UNSIGNED NOT NULL COMMENT '区域ID，关联 base_zone.id',
  `shelf_no`           INT             NOT NULL COMMENT '货架号',
  `layer_no`           INT             NOT NULL COMMENT '层号',
  `column_no`          INT             NOT NULL COMMENT '列号',
  `position_no`        INT             NOT NULL COMMENT '位号',
  `location_type`      VARCHAR(20)     NOT NULL DEFAULT 'NORMAL' COMMENT '库位类型：NORMAL普通 TEMP暂存 DEFECT不良品 RETURN_PENDING退货待检',
  `status`             VARCHAR(20)     NOT NULL DEFAULT 'FREE' COMMENT '库位状态：FREE空闲 OCCUPIED占用 LOCKED锁定 MAINTENANCE维修中',
  `max_weight`         DECIMAL(10,2)   NOT NULL DEFAULT 0 COMMENT '最大承重（kg）',
  `max_volume`         DECIMAL(10,3)   NOT NULL DEFAULT 0 COMMENT '最大体积（m³）',
  `used_weight`        DECIMAL(10,2)   NOT NULL DEFAULT 0 COMMENT '已用承重（kg）',
  `used_volume`        DECIMAL(10,3)   NOT NULL DEFAULT 0 COMMENT '已用体积（m³）',
  `lock_reason`        VARCHAR(200)        DEFAULT NULL COMMENT '锁定/维修原因',
  `status_update_time` DATETIME            DEFAULT NULL COMMENT '状态变更时间',
  `create_by`          VARCHAR(50)         DEFAULT NULL COMMENT '创建人',
  `create_time`        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`          VARCHAR(50)         DEFAULT NULL COMMENT '修改人',
  `update_time`        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted`            TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_location_code` (`location_code`),
  KEY `idx_warehouse_zone` (`warehouse_id`, `zone_id`),
  KEY `idx_status` (`status`),
  KEY `idx_location_type` (`location_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库位主数据表';

-- -------------------------------------------------------------------
-- 17. base_supplier 供应商表
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `base_supplier` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `supplier_code`  VARCHAR(32)     NOT NULL COMMENT '供应商编码（唯一）',
  `supplier_name`  VARCHAR(200)    NOT NULL COMMENT '供应商名称',
  `contact_name`   VARCHAR(50)         DEFAULT NULL COMMENT '联系人',
  `contact_phone`  VARCHAR(20)         DEFAULT NULL COMMENT '联系电话',
  `address`        VARCHAR(500)        DEFAULT NULL COMMENT '地址',
  `remark`         VARCHAR(500)        DEFAULT NULL COMMENT '备注',
  `status`         TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `create_by`      VARCHAR(50)         DEFAULT NULL COMMENT '创建人',
  `create_time`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`      VARCHAR(50)         DEFAULT NULL COMMENT '修改人',
  `update_time`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted`        TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_code` (`supplier_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商基础数据表';

SET FOREIGN_KEY_CHECKS = 1;

-- ===================================================================
-- 建表完成：共 17 张表
-- 下一步：执行 R1-V1.0.1-init-data.sql 初始化系统数据
-- ===================================================================
