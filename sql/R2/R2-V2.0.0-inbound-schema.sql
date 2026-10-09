-- WMS R2 入库域建表：inbound_order / inbound_item / inbound_qc / putaway_task
SET NAMES utf8mb4; SET FOREIGN_KEY_CHECKS = 0; USE `wms`;
CREATE TABLE IF NOT EXISTS `inbound_order` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `inbound_no` VARCHAR(32) NOT NULL COMMENT '入库单号',
    `warehouse_id` BIGINT UNSIGNED NOT NULL COMMENT '仓库ID',
    `supplier_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '供应商ID',
    `inbound_type` VARCHAR(20) NOT NULL DEFAULT 'PURCHASE' COMMENT 'PURCHASE/RETURN/TRANSFER',
    `expected_time` DATETIME DEFAULT NULL COMMENT '预计到货',
    `arrive_time` DATETIME DEFAULT NULL COMMENT '实际到货',
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/CONFIRMED/ARRIVED/PENDING_QC/PUTTING_AWAY/COMPLETED/CANCELLED',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_by` VARCHAR(50) DEFAULT NULL, `update_by` VARCHAR(50) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`), UNIQUE KEY `uk_inbound_no` (`inbound_no`),
    KEY `idx_warehouse_id` (`warehouse_id`), KEY `idx_supplier_id` (`supplier_id`), KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库预约单';
CREATE TABLE IF NOT EXISTS `inbound_item` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `inbound_id` BIGINT UNSIGNED NOT NULL COMMENT '入库单ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `plan_qty` INT NOT NULL DEFAULT 0 COMMENT '计划数量',
    `received_qty` INT NOT NULL DEFAULT 0 COMMENT '实收数量',
    `qualified_qty` INT NOT NULL DEFAULT 0 COMMENT '合格数量',
    `defect_qty` INT NOT NULL DEFAULT 0 COMMENT '不合格数量',
    `putaway_qty` INT NOT NULL DEFAULT 0 COMMENT '已上架数量',
    `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    `production_date` DATE DEFAULT NULL COMMENT '生产日期', `expiry_date` DATE DEFAULT NULL COMMENT '到期日期',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`), KEY `idx_inbound_id` (`inbound_id`), KEY `idx_sku_id` (`sku_id`), KEY `idx_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库明细';
CREATE TABLE IF NOT EXISTS `inbound_qc` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `inbound_id` BIGINT UNSIGNED NOT NULL COMMENT '入库单ID',
    `item_id` BIGINT UNSIGNED NOT NULL COMMENT '入库明细ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    `inspect_qty` INT NOT NULL DEFAULT 0 COMMENT '送检数量',
    `qualified_qty` INT NOT NULL DEFAULT 0 COMMENT '合格数量',
    `defect_qty` INT NOT NULL DEFAULT 0 COMMENT '不合格数量',
    `grade` VARCHAR(2) DEFAULT NULL COMMENT '等级A/B/C',
    `defect_reason` VARCHAR(100) DEFAULT NULL COMMENT '不合格原因',
    `handle_way` VARCHAR(20) DEFAULT NULL COMMENT 'REJECT/DEFECT_LOCATION',
    `inspector` VARCHAR(50) DEFAULT NULL COMMENT '质检人', `inspect_time` DATETIME DEFAULT NULL COMMENT '质检时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`), KEY `idx_inbound_id` (`inbound_id`), KEY `idx_item_id` (`item_id`), KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库质检记录';
CREATE TABLE IF NOT EXISTS `putaway_task` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `task_no` VARCHAR(32) NOT NULL COMMENT '上架任务号',
    `inbound_id` BIGINT UNSIGNED NOT NULL COMMENT '入库单ID',
    `item_id` BIGINT UNSIGNED NOT NULL COMMENT '入库明细ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `warehouse_id` BIGINT UNSIGNED NOT NULL COMMENT '仓库ID',
    `location_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '目标库位ID',
    `plan_qty` INT NOT NULL DEFAULT 0 COMMENT '计划上架数量',
    `actual_qty` INT NOT NULL DEFAULT 0 COMMENT '实际上架数量',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PUTTING/COMPLETED/CANCELLED',
    `operator` VARCHAR(50) DEFAULT NULL COMMENT '上架人', `operate_time` DATETIME DEFAULT NULL COMMENT '上架时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`), UNIQUE KEY `uk_task_no` (`task_no`),
    KEY `idx_inbound_id` (`inbound_id`), KEY `idx_sku_id` (`sku_id`), KEY `idx_location_id` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库上架任务';
