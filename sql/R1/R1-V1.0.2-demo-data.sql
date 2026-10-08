-- ===================================================================
-- WMS 电商仓储管理系统
-- 版本：R1（基础平台与主数据）
-- 脚本：R1-V1.0.2-demo-data.sql  演示数据（可选执行）
-- 数据库：MySQL 8.0+
-- 前置：需先执行 R1-V1.0.0-schema.sql 与 R1-V1.0.1-init-data.sql
-- 说明：用于开发/测试/验收环境的基础业务演示数据
--       与本文件配套的验收预置账号见
--       需求分析/验收标准/R1-基础平台与主数据验收标准.md 第 1.2 / 1.3 节
-- ===================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `wms`;

-- 角色 ID（从初始化数据中查询）
SET @role_supervisor := (SELECT `id` FROM `sys_role` WHERE `role_code` = 'WAREHOUSE_SUPERVISOR');
SET @role_wh_admin   := (SELECT `id` FROM `sys_role` WHERE `role_code` = 'WAREHOUSE_ADMIN');
SET @role_picker     := (SELECT `id` FROM `sys_role` WHERE `role_code` = 'PICKER');
SET @role_reviewer   := (SELECT `id` FROM `sys_role` WHERE `role_code` = 'REVIEWER');


-- ===================================================================
-- 一、仓库（4 个，含 1 个停用仓库用于停用场景验证）
-- ===================================================================
INSERT INTO `base_warehouse` (`warehouse_code`, `warehouse_name`, `warehouse_type`, `region`, `address`, `contact_name`, `contact_phone`, `status`, `remark`, `create_by`)
VALUES ('WH-001', '广州中心仓', 'CENTRAL', '华南', '广东省广州市白云区XX路1号', '李四', '13800138001', 1, '主仓，承担主要出库作业', 'system');
SET @wh1 := LAST_INSERT_ID();

INSERT INTO `base_warehouse` (`warehouse_code`, `warehouse_name`, `warehouse_type`, `region`, `address`, `contact_name`, `contact_phone`, `status`, `remark`, `create_by`)
VALUES ('WH-002', '上海区域仓', 'REGIONAL', '华东', '上海市闵行区XX路2号', '周八', '13800138005', 1, '华东区域分仓', 'system');
SET @wh2 := LAST_INSERT_ID();

INSERT INTO `base_warehouse` (`warehouse_code`, `warehouse_name`, `warehouse_type`, `region`, `address`, `contact_name`, `contact_phone`, `status`, `remark`, `create_by`)
VALUES ('WH-003', '美国洛杉矶仓', 'OVERSEAS', '北美', 'Los Angeles, CA, USA', 'Smith', '001-909-123456', 1, '北美海外仓', 'system');
SET @wh3 := LAST_INSERT_ID();

INSERT INTO `base_warehouse` (`warehouse_code`, `warehouse_name`, `warehouse_type`, `region`, `address`, `status`, `remark`, `create_by`)
VALUES ('WH-004', '成都区域仓', 'REGIONAL', '西南', '四川省成都市XX路3号', 0, '已停用，用于验证停仓后不可创建单据', 'system');


-- ===================================================================
-- 二、仓库区域（WH-001 下 3 个区域）
-- ===================================================================
INSERT INTO `base_zone` (`warehouse_id`, `zone_code`, `zone_name`, `zone_type`, `turnover_zone`, `status`, `remark`, `create_by`)
VALUES (@wh1, 'A', 'A 区', 'STORAGE', 'FAST', 1, '靠近打包区，存放高频商品', 'system');
SET @zone_a := LAST_INSERT_ID();

INSERT INTO `base_zone` (`warehouse_id`, `zone_code`, `zone_name`, `zone_type`, `turnover_zone`, `status`, `remark`, `create_by`)
VALUES (@wh1, 'B', 'B 区', 'STORAGE', 'SLOW', 1, '远端区域，存放中低频商品', 'system');
SET @zone_b := LAST_INSERT_ID();

INSERT INTO `base_zone` (`warehouse_id`, `zone_code`, `zone_name`, `zone_type`, `turnover_zone`, `status`, `remark`, `create_by`)
VALUES (@wh1, 'C', 'C 区', 'RETURN', NULL, 1, '退货区，存放待质检退货', 'system');
SET @zone_c := LAST_INSERT_ID();


-- ===================================================================
-- 三、库位（A 区批量生成 5 货架 × 3 层 × 10 列 = 150 个）
-- 编码规则：{仓库编码}-{区域}-{货架}-{层}-{列}-{位}
-- ===================================================================
INSERT INTO `base_location`
  (`location_code`, `warehouse_id`, `zone_id`, `shelf_no`, `layer_no`, `column_no`, `position_no`,
   `location_type`, `status`, `max_weight`, `max_volume`, `create_by`)
SELECT
  CONCAT('WH-001-A-', LPAD(s.n, 2, '0'), '-', LPAD(l.n, 2, '0'), '-', LPAD(c.n, 2, '0'), '-01'),
  @wh1, @zone_a, s.n, l.n, c.n, 1,
  'NORMAL', 'FREE', 500.00, 2.000, 'system'
FROM
  (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) s
CROSS JOIN
  (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3) l
CROSS JOIN
  (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
   UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) c;

-- B 区 20 个库位（含 1 个不良品库位）
INSERT INTO `base_location`
  (`location_code`, `warehouse_id`, `zone_id`, `shelf_no`, `layer_no`, `column_no`, `position_no`,
   `location_type`, `status`, `max_weight`, `max_volume`, `create_by`)
SELECT
  CONCAT('WH-001-B-01-', LPAD(l.n, 2, '0'), '-', LPAD(c.n, 2, '0'), '-01'),
  @wh1, @zone_b, 1, l.n, c.n, 1,
  'NORMAL', 'FREE', 500.00, 2.000, 'system'
FROM
  (SELECT 1 n UNION ALL SELECT 2) l
CROSS JOIN
  (SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
   UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) c;

-- C 区退货区库位（含退货待检类型）
INSERT INTO `base_location`
  (`location_code`, `warehouse_id`, `zone_id`, `shelf_no`, `layer_no`, `column_no`, `position_no`,
   `location_type`, `status`, `max_weight`, `max_volume`, `create_by`) VALUES
('WH-001-C-01-01-01-01', @wh1, @zone_c, 1, 1, 1, 1, 'RETURN_PENDING', 'FREE',     300.00, 1.500, 'system'),
('WH-001-C-01-01-02-01', @wh1, @zone_c, 1, 1, 2, 1, 'RETURN_PENDING', 'FREE',     300.00, 1.500, 'system'),
('WH-001-B-99-01-01-01', @wh1, @zone_b, 99, 1, 1, 1, 'DEFECT',        'FREE',     300.00, 1.500, 'system');
-- 注：A 区的 01-01-01-01 已由上方批量生成包含，此处不再重复插入以免违反 uk_location_code

-- 演示用特殊状态库位
UPDATE `base_location` SET `status` = 'OCCUPIED', `used_weight` = 19.00, `used_volume` = 0.048
WHERE `location_code` = 'WH-001-A-01-01-01-01';
UPDATE `base_location` SET `status` = 'OCCUPIED', `used_weight` = 45.00, `used_volume` = 0.120
WHERE `location_code` = 'WH-001-A-01-02-01-01';
UPDATE `base_location` SET `status` = 'LOCKED', `lock_reason` = '货架检修，暂停使用', `status_update_time` = NOW()
WHERE `location_code` = 'WH-001-A-01-03-01-01';
UPDATE `base_location` SET `status` = 'MAINTENANCE', `lock_reason` = '立柱变形，待维修', `status_update_time` = NOW()
WHERE `location_code` = 'WH-001-A-01-03-02-01';


-- ===================================================================
-- 四、商品类目
-- ===================================================================
INSERT INTO `base_category` (`category_code`, `category_name`, `parent_id`, `level`, `path`, `sort`, `status`, `create_by`) VALUES
('CAT_001', '女装', 0, 1, '0',  1, 1, 'system');
SET @cat_women := LAST_INSERT_ID();

INSERT INTO `base_category` (`category_code`, `category_name`, `parent_id`, `level`, `path`, `sort`, `status`, `create_by`) VALUES
('CAT_002', '男装', 0, 1, '0',  2, 1, 'system');
SET @cat_men := LAST_INSERT_ID();

INSERT INTO `base_category` (`category_code`, `category_name`, `parent_id`, `level`, `path`, `sort`, `status`, `create_by`) VALUES
('CAT_001_01', '连衣裙', @cat_women, 2, CONCAT('0/', @cat_women), 1, 1, 'system');
SET @cat_dress := LAST_INSERT_ID();

INSERT INTO `base_category` (`category_code`, `category_name`, `parent_id`, `level`, `path`, `sort`, `status`, `create_by`) VALUES
('CAT_002_01', 'T恤',  @cat_men, 2, CONCAT('0/', @cat_men), 1, 1, 'system');
SET @cat_tshirt := LAST_INSERT_ID();

INSERT INTO `base_category` (`category_code`, `category_name`, `parent_id`, `level`, `path`, `sort`, `status`, `create_by`) VALUES
('CAT_002_02', '裤装', @cat_men, 2, CONCAT('0/', @cat_men), 2, 1, 'system');
SET @cat_pants := LAST_INSERT_ID();


-- ===================================================================
-- 五、SKU 及条形码
-- ===================================================================
INSERT INTO `base_sku`
  (`sku_code`, `spu_code`, `sku_name`, `color`, `size`, `category_id`, `image_url`,
   `weight`, `volume`, `length_mm`, `width_mm`, `height_mm`, `shelf_life_days`, `status`, `created_from`, `create_by`) VALUES
('SP001-RED-S',  'SP001', '夏季连衣裙', '红色', 'S',  @cat_dress,  'https://oss.example.com/sku/SP001-RED-S.jpg',  200.00, 500.00, 300, 200, 50, NULL, 1, 'MANUAL', 'system');
SET @sku1 := LAST_INSERT_ID();

INSERT INTO `base_sku`
  (`sku_code`, `spu_code`, `sku_name`, `color`, `size`, `category_id`,
   `weight`, `volume`, `length_mm`, `width_mm`, `height_mm`, `status`, `created_from`, `create_by`) VALUES
('SP001-RED-M',  'SP001', '夏季连衣裙', '红色', 'M',  @cat_dress,  210.00, 520.00, 300, 200, 50, 1, 'MANUAL', 'system');
SET @sku2 := LAST_INSERT_ID();

INSERT INTO `base_sku`
  (`sku_code`, `spu_code`, `sku_name`, `color`, `size`, `category_id`,
   `weight`, `volume`, `length_mm`, `width_mm`, `height_mm`, `status`, `created_from`, `create_by`) VALUES
('SP002-BLU-L',  'SP002', 'T 恤',      '蓝色', 'L',  @cat_tshirt, 150.00, 300.00, 250, 180, 30, 0, 'MANUAL', 'system');
SET @sku3 := LAST_INSERT_ID();

INSERT INTO `base_sku`
  (`sku_code`, `spu_code`, `sku_name`, `color`, `size`, `category_id`,
   `weight`, `volume`, `length_mm`, `width_mm`, `height_mm`, `shelf_life_days`, `status`, `created_from`, `create_by`) VALUES
('SP003-BLK-M',  'SP003', '牛仔裤',    '黑色', 'M',  @cat_pants,  400.00, 800.00, 350, 220, 60, NULL, 1, 'MANUAL', 'system');
SET @sku4 := LAST_INSERT_ID();

INSERT INTO `base_sku`
  (`sku_code`, `spu_code`, `sku_name`, `color`, `size`, `category_id`,
   `weight`, `volume`, `length_mm`, `width_mm`, `height_mm`, `shelf_life_days`, `status`, `created_from`, `create_by`) VALUES
('SP004-WHT-XL', 'SP004', '纯棉衬衫',  '白色', 'XL', @cat_tshirt, 180.00, 400.00, 280, 200, 40, 730, 1, 'IMPORT', 'system');
SET @sku5 := LAST_INSERT_ID();

-- 条形码（SP003 演示一品多码）
INSERT INTO `base_sku_barcode` (`sku_id`, `barcode`, `package_spec`, `status`, `create_by`) VALUES
(@sku1, '6901234567890', '单件装', 1, 'system'),
(@sku2, '6901234567891', '单件装', 1, 'system'),
(@sku3, '6901234567892', '单件装', 1, 'system'),
(@sku4, '6901234567893', '单件装', 1, 'system'),
(@sku4, '6901234567894', '3件装',  1, 'system'),
(@sku5, '6901234567895', '单件装', 1, 'system');


-- ===================================================================
-- 六、供应商（3 个，含 1 个停用）
-- ===================================================================
INSERT INTO `base_supplier`
  (`supplier_code`, `supplier_name`, `contact_name`, `contact_phone`, `address`, `status`, `remark`, `create_by`) VALUES
('SUP001', 'XX 供应商', '陈一', '13800138001', '广东省广州市白云区XX路1号', 1, '主营女装', 'system'),
('SUP002', 'YY 供应商', '陈二', '13800138002', '上海市闵行区XX路2号',       1, '主营男装', 'system'),
('SUP003', 'ZZ 供应商', '陈三', '13800138003', '四川省成都市XX路3号',       0, '已停用',   'system');


-- ===================================================================
-- 七、测试账号（对应验收标准的预置账号）
-- 密码：初始密码 123456，{noop} 表示明文（仅测试环境）
-- role_id 引用已初始化的角色
-- ===================================================================
INSERT INTO `sys_user`
  (`username`, `real_name`, `password`, `role_id`, `warehouse_id`, `phone`, `email`, `status`, `force_change_password`, `create_by`) VALUES
('supervisor01', '李四', '{noop}123456', @role_supervisor, @wh1, '13800138011', 'supervisor01@wms.com', 1, 0, 'system'),
('whadmin01',    '王五', '{noop}123456', @role_wh_admin,   @wh1, '13800138012', 'whadmin01@wms.com',    1, 0, 'system'),
('picker01',     '赵六', '{noop}123456', @role_picker,     @wh1, '13800138013', 'picker01@wms.com',     1, 0, 'system'),
('reviewer01',   '孙七', '{noop}123456', @role_reviewer,   @wh1, '13800138014', 'reviewer01@wms.com',   0, 0, 'system');


SET FOREIGN_KEY_CHECKS = 1;

-- ===================================================================
-- 演示数据导入完成
--   仓库 4 个（含 1 个停用）｜区域 3 个｜库位约 175 个（含占用/锁定/维修示例）
--   类目 5 个｜SKU 5 个（含 1 个停用）｜条形码 6 个（含一品多码示例）
--   供应商 3 个（含 1 个停用）｜测试账号 4 个（reviewer01 为停用状态）
--
-- 账号一览：
--   admin        / 123456  系统管理员（首次登录须改密）
--   supervisor01 / 123456  库管主管
--   whadmin01    / 123456  仓库管理员
--   picker01     / 123456  拣货员
--   reviewer01   / 123456  复核员（停用，用于验证停用账户不可登录）
-- ===================================================================
