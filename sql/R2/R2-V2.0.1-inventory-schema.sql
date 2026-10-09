-- WMS R2 库存域建表：inv_stock / inv_transaction / inv_adjust / inv_adjust_item / inv_warning_rule / inv_warning_record
SET NAMES utf8mb4; SET FOREIGN_KEY_CHECKS = 0; USE `wms`;
CREATE TABLE IF NOT EXISTS `inv_stock` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `warehouse_id` BIGINT UNSIGNED NOT NULL COMMENT '仓库ID',
    `location_id` BIGINT UNSIGNED NOT NULL COMMENT '库位ID',
    `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    `production_date` DATE DEFAULT NULL COMMENT '生产日期', `expiry_date` DATE DEFAULT NULL COMMENT '到期日期',
    `total_qty` INT NOT NULL DEFAULT 0 COMMENT '总库存', `available_qty` INT NOT NULL DEFAULT 0 COMMENT '可用库存',
    `locked_qty` INT NOT NULL DEFAULT 0 COMMENT '锁定库存', `frozen_qty` INT NOT NULL DEFAULT 0 COMMENT '冻结库存',
    `status` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL/FROZEN',
    `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`), UNIQUE KEY `uk_stock` (`sku_id`,`warehouse_id`,`location_id`,`batch_no`,`deleted`),
    KEY `idx_warehouse_id` (`warehouse_id`), KEY `idx_location_id` (`location_id`), KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存';
CREATE TABLE IF NOT EXISTS `inv_transaction` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `transaction_no` VARCHAR(32) NOT NULL COMMENT '流水号',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `warehouse_id` BIGINT UNSIGNED NOT NULL COMMENT '仓库ID',
    `location_id` BIGINT UNSIGNED NOT NULL COMMENT '库位ID',
    `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    `transaction_type` VARCHAR(20) NOT NULL COMMENT 'INBOUND/OUTBOUND/ADJUST/LOCK/UNLOCK/FREEZE',
    `quantity` INT NOT NULL DEFAULT 0 COMMENT '变动数量',
    `before_qty` INT NOT NULL DEFAULT 0 COMMENT '变动前可用', `after_qty` INT NOT NULL DEFAULT 0 COMMENT '变动后可用',
    `related_no` VARCHAR(64) DEFAULT NULL COMMENT '关联单号', `operator` VARCHAR(50) DEFAULT NULL COMMENT '操作人',
    `operate_time` DATETIME DEFAULT NULL COMMENT '操作时间', `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`), UNIQUE KEY `uk_transaction_no` (`transaction_no`),
    KEY `idx_sku_id` (`sku_id`), KEY `idx_warehouse_id` (`warehouse_id`), KEY `idx_related_no` (`related_no`), KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水';
CREATE TABLE IF NOT EXISTS `inv_adjust` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `adjust_no` VARCHAR(32) NOT NULL COMMENT '调整单号',
    `warehouse_id` BIGINT UNSIGNED NOT NULL COMMENT '仓库ID',
    `reason` VARCHAR(20) NOT NULL DEFAULT 'OTHER' COMMENT 'PROFIT/LOSS/CHECK/OTHER',
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PENDING/APPROVED/REJECTED',
    `operator` VARCHAR(50) DEFAULT NULL COMMENT '制单人', `auditor` VARCHAR(50) DEFAULT NULL COMMENT '审批人',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审批时间', `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`), UNIQUE KEY `uk_adjust_no` (`adjust_no`), KEY `idx_warehouse_id` (`warehouse_id`), KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调整单';
CREATE TABLE IF NOT EXISTS `inv_adjust_item` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `adjust_id` BIGINT UNSIGNED NOT NULL COMMENT '调整单ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `location_id` BIGINT UNSIGNED NOT NULL COMMENT '库位ID',
    `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    `before_qty` INT NOT NULL DEFAULT 0 COMMENT '调整前', `after_qty` INT NOT NULL DEFAULT 0 COMMENT '调整后',
    `diff_qty` INT NOT NULL DEFAULT 0 COMMENT '差异', `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`), KEY `idx_adjust_id` (`adjust_id`), KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调整单明细';
CREATE TABLE IF NOT EXISTS `inv_warning_rule` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `sku_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'SKU ID（空=全仓）',
    `warehouse_id` BIGINT UNSIGNED NOT NULL COMMENT '仓库ID',
    `warning_type` VARCHAR(20) NOT NULL COMMENT 'LOW/HIGH/EXPIRE/DEAD',
    `min_qty` INT DEFAULT NULL COMMENT '低库存阈值', `max_qty` INT DEFAULT NULL COMMENT '高库存阈值',
    `expire_days` INT DEFAULT NULL COMMENT '临期天数', `dead_days` INT DEFAULT NULL COMMENT '呆滞天数',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '启用', `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`), KEY `idx_warehouse_id` (`warehouse_id`), KEY `idx_sku_id` (`sku_id`), KEY `idx_warning_type` (`warning_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预警规则';
CREATE TABLE IF NOT EXISTS `inv_warning_record` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `rule_id` BIGINT UNSIGNED NOT NULL COMMENT '规则ID',
    `sku_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'SKU ID',
    `warehouse_id` BIGINT UNSIGNED NOT NULL COMMENT '仓库ID',
    `warning_type` VARCHAR(20) NOT NULL COMMENT 'LOW/HIGH/EXPIRE/DEAD',
    `current_qty` INT NOT NULL DEFAULT 0 COMMENT '触发时库存', `threshold_value` INT DEFAULT NULL COMMENT '阈值',
    `status` VARCHAR(20) NOT NULL DEFAULT 'UNHANDLED' COMMENT 'UNHANDLED/HANDLED/IGNORED',
    `trigger_time` DATETIME DEFAULT NULL COMMENT '触发时间', `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `handle_by` VARCHAR(50) DEFAULT NULL COMMENT '处理人', `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`), KEY `idx_rule_id` (`rule_id`), KEY `idx_warehouse_id` (`warehouse_id`), KEY `idx_sku_id` (`sku_id`), KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预警记录';
