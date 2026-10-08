-- ===================================================================
-- WMS 电商仓储管理系统
-- 版本：R1（基础平台与主数据）
-- 脚本：R1-V1.0.1-init-data.sql  系统初始化数据
-- 数据库：MySQL 8.0+
-- 前置：需先执行 R1-V1.0.0-schema.sql
-- 说明：初始化角色、权限、角色权限关系、数据字典、编码规则、系统参数、超级管理员
-- ===================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `wms`;

-- ===================================================================
-- 一、角色数据（5 个预置角色）
-- ===================================================================
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `builtin`, `status`, `create_by`)
VALUES ('SYSTEM_ADMIN', '系统管理员', '拥有系统全部权限，负责用户/权限与基础数据维护', 1, 1, 'system');
SET @role_admin := LAST_INSERT_ID();

INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `builtin`, `status`, `create_by`) VALUES
('WAREHOUSE_SUPERVISOR', '库管主管', '负责跨仓查询、异常审批、盘点任务发起、库存调整审批', 1, 1, 'system');
SET @role_supervisor := LAST_INSERT_ID();

INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `builtin`, `status`, `create_by`) VALUES
('WAREHOUSE_ADMIN', '仓库管理员', '负责入库/出库/盘点单据处理、库位分配、入库验收', 1, 1, 'system');
SET @role_wh_admin := LAST_INSERT_ID();

INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `builtin`, `status`, `create_by`) VALUES
('PICKER', '拣货员', '负责接取拣货任务、按路径拣货、扫码确认、上架与盘点执行', 1, 1, 'system');
SET @role_picker := LAST_INSERT_ID();

INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `builtin`, `status`, `create_by`) VALUES
('REVIEWER', '复核员', '负责复核拣货商品、打包、贴运单', 1, 1, 'system');
SET @role_reviewer := LAST_INSERT_ID();


-- ===================================================================
-- 二、权限数据（菜单 + 按钮）
-- 说明：逐条插入菜单并保存 ID，按钮权限挂载到对应菜单下
-- R2-R5 相关权限一并预置，在对应版本上线后生效
-- ===================================================================

-- ---------- 顶级菜单 ----------
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`, `remark`)
VALUES ('MENU_DASHBOARD', '主控台', 0, 'MENU', '/dashboard', 'dashboard', 1, '运营总览看板');
SET @p_dashboard := LAST_INSERT_ID();

INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`, `remark`)
VALUES ('MENU_BASIC', '基础数据', 0, 'MENU', '/basic', 'database', 10, 'SKU/仓库/库位/供应商');
SET @p_basic := LAST_INSERT_ID();

INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`, `remark`)
VALUES ('MENU_SYSTEM', '系统管理', 0, 'MENU', '/system', 'setting', 20, '用户/角色/配置');
SET @p_system := LAST_INSERT_ID();

INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`, `remark`)
VALUES ('MENU_LOG', '日志中心', 0, 'MENU', '/log', 'file-text', 30, '操作日志/登录日志');
SET @p_log := LAST_INSERT_ID();

-- ---------- 基础数据二级菜单 ----------
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`)
VALUES ('MENU_SKU', 'SKU 管理', @p_basic, 'MENU', '/basic/sku', 'goods', 11);
SET @p_menu_sku := LAST_INSERT_ID();

INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`)
VALUES ('MENU_WAREHOUSE', '仓库管理', @p_basic, 'MENU', '/basic/warehouse', 'home', 12);
SET @p_menu_wh := LAST_INSERT_ID();

INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`)
VALUES ('MENU_LOCATION', '库位管理', @p_basic, 'MENU', '/basic/location', 'grid', 13);
SET @p_menu_loc := LAST_INSERT_ID();

INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`)
VALUES ('MENU_SUPPLIER', '供应商管理', @p_basic, 'MENU', '/basic/supplier', 'truck', 14);
SET @p_menu_sup := LAST_INSERT_ID();

-- ---------- 系统管理二级菜单 ----------
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`)
VALUES ('MENU_USER', '用户管理', @p_system, 'MENU', '/system/user', 'user', 21);
SET @p_menu_user := LAST_INSERT_ID();

INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`)
VALUES ('MENU_ROLE', '角色权限', @p_system, 'MENU', '/system/role', 'safety', 22);
SET @p_menu_role := LAST_INSERT_ID();

INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`)
VALUES ('MENU_CONFIG', '系统配置', @p_system, 'MENU', '/system/config', 'tool', 23);
SET @p_menu_cfg := LAST_INSERT_ID();

-- ---------- 日志二级菜单 ----------
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`)
VALUES ('MENU_OP_LOG', '操作日志', @p_log, 'MENU', '/log/oplog', 'history', 31);
SET @p_menu_oplog := LAST_INSERT_ID();

INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`)
VALUES ('MENU_LOGIN_LOG', '登录日志', @p_log, 'MENU', '/log/loginlog', 'login', 32);
SET @p_menu_loginlog := LAST_INSERT_ID();

-- ---------- 后续版本菜单（预置，对应版本上线后生效） ----------
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`, `remark`)
VALUES ('MENU_INBOUND', '入库管理', 0, 'MENU', '/inbound', 'import', 40, 'R2 生效');
SET @p_menu_inbound := LAST_INSERT_ID();

INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`, `remark`)
VALUES ('MENU_INVENTORY', '库存管理', 0, 'MENU', '/inventory', 'inbox', 41, 'R2 生效');
SET @p_menu_inv := LAST_INSERT_ID();

INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`, `remark`)
VALUES ('MENU_OUTBOUND', '出库管理', 0, 'MENU', '/outbound', 'export', 42, 'R3 生效');
SET @p_menu_outbound := LAST_INSERT_ID();

INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`, `remark`)
VALUES ('MENU_STOCKTAKE', '盘点管理', 0, 'MENU', '/stocktake', 'check', 43, 'R4 生效');
SET @p_menu_st := LAST_INSERT_ID();

INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`, `remark`)
VALUES ('MENU_REPORT', '报表中心', 0, 'MENU', '/report', 'chart', 44, 'R4 生效');
SET @p_menu_report := LAST_INSERT_ID();

INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `path`, `icon`, `sort`, `remark`)
VALUES ('MENU_RETURN', '退货管理', 0, 'MENU', '/return', 'rollback', 45, 'R4 生效');
SET @p_menu_return := LAST_INSERT_ID();

-- ---------- 按钮权限：主控台 ----------
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `sort`) VALUES
('dashboard:view', '查看', @p_dashboard, 'BUTTON', 1);

-- ---------- 按钮权限：SKU ----------
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `sort`) VALUES
('sku:view',   '查询', @p_menu_sku, 'BUTTON', 1),
('sku:create', '创建', @p_menu_sku, 'BUTTON', 2),
('sku:edit',   '编辑', @p_menu_sku, 'BUTTON', 3),
('sku:delete', '删除', @p_menu_sku, 'BUTTON', 4),
('sku:import', '导入', @p_menu_sku, 'BUTTON', 5),
('sku:export', '导出', @p_menu_sku, 'BUTTON', 6),
('sku:enable', '启用停用', @p_menu_sku, 'BUTTON', 7);

-- ---------- 按钮权限：仓库 ----------
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `sort`) VALUES
('warehouse:view',   '查询', @p_menu_wh, 'BUTTON', 1),
('warehouse:create', '创建', @p_menu_wh, 'BUTTON', 2),
('warehouse:edit',   '编辑', @p_menu_wh, 'BUTTON', 3),
('warehouse:zone',   '区域管理', @p_menu_wh, 'BUTTON', 4);

-- ---------- 按钮权限：库位 ----------
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `sort`) VALUES
('location:view',   '查询', @p_menu_loc, 'BUTTON', 1),
('location:create', '创建', @p_menu_loc, 'BUTTON', 2),
('location:batch',  '批量创建', @p_menu_loc, 'BUTTON', 3),
('location:edit',   '编辑', @p_menu_loc, 'BUTTON', 4),
('location:status', '状态变更', @p_menu_loc, 'BUTTON', 5);

-- ---------- 按钮权限：供应商 ----------
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `sort`) VALUES
('supplier:view',   '查询', @p_menu_sup, 'BUTTON', 1),
('supplier:create', '创建', @p_menu_sup, 'BUTTON', 2),
('supplier:edit',   '编辑', @p_menu_sup, 'BUTTON', 3);

-- ---------- 按钮权限：用户/角色/配置 ----------
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `sort`) VALUES
('user:view',   '查询', @p_menu_user, 'BUTTON', 1),
('user:create', '创建', @p_menu_user, 'BUTTON', 2),
('user:edit',   '编辑', @p_menu_user, 'BUTTON', 3),
('user:delete', '删除', @p_menu_user, 'BUTTON', 4),
('user:reset',  '重置密码', @p_menu_user, 'BUTTON', 5);

INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `sort`) VALUES
('role:view', '查询', @p_menu_role, 'BUTTON', 1),
('role:edit', '配置权限', @p_menu_role, 'BUTTON', 2);

INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `sort`) VALUES
('config:view', '查询', @p_menu_cfg, 'BUTTON', 1),
('config:edit', '修改', @p_menu_cfg, 'BUTTON', 2);

-- ---------- 按钮权限：日志 ----------
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `sort`) VALUES
('log:view', '查看日志', @p_log, 'BUTTON', 1);

-- ---------- 按钮权限：后续版本（预置） ----------
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `parent_id`, `permission_type`, `sort`, `remark`) VALUES
('inbound:create',   '创建入库单', @p_menu_inbound, 'BUTTON', 1, 'R2'),
('inbound:receive',  '到货验收',   @p_menu_inbound, 'BUTTON', 2, 'R2'),
('inbound:qc',       '质检',       @p_menu_inbound, 'BUTTON', 3, 'R2'),
('putaway:execute',  '上架确认',   @p_menu_inbound, 'BUTTON', 4, 'R2'),
('inventory:view',   '库存查询',   @p_menu_inv,     'BUTTON', 1, 'R2'),
('inventory:adjust', '库存调整',   @p_menu_inv,     'BUTTON', 2, 'R2'),
('outbound:view',    '出库单查询', @p_menu_outbound,'BUTTON', 1, 'R3'),
('wave:create',      '波次生成',   @p_menu_outbound,'BUTTON', 2, 'R3'),
('pick:execute',     '拣货',       @p_menu_outbound,'BUTTON', 3, 'R3'),
('review:execute',   '复核打包',   @p_menu_outbound,'BUTTON', 4, 'R3'),
('stocktake:create', '创建盘点单', @p_menu_st,      'BUTTON', 1, 'R4'),
('stocktake:execute','盘点执行',   @p_menu_st,      'BUTTON', 2, 'R4'),
('stocktake:audit',  '差异审批',   @p_menu_st,      'BUTTON', 3, 'R4'),
('report:view',      '报表查看',   @p_menu_report,  'BUTTON', 1, 'R4'),
('return:view',      '退货查询',   @p_menu_return,  'BUTTON', 1, 'R4'),
('return:inspect',   '退货质检',   @p_menu_return,  'BUTTON', 2, 'R4');


-- ===================================================================
-- 三、角色权限关系
-- 依据 PRD 五、权限矩阵（p.119）与 wms-prd-v2.md 4.2 增量矩阵
-- ===================================================================

-- 3.1 系统管理员：全部权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT @role_admin, `id` FROM `sys_permission` WHERE `deleted` = 0;

-- 3.2 库管主管：跨仓查询、业务单据处理与审批
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT @role_supervisor, `id` FROM `sys_permission`
WHERE `permission_code` IN (
  'dashboard:view',
  'sku:view',
  'warehouse:view',
  'location:view',
  'supplier:view',
  'inbound:create',
  'outbound:view',
  'wave:create',
  'inventory:view',
  'inventory:adjust',
  'stocktake:create',
  'stocktake:audit',
  'report:view',
  'return:view'
);

-- 3.3 仓库管理员：入库/出库/盘点单据执行，SKU 仅查询
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT @role_wh_admin, `id` FROM `sys_permission`
WHERE `permission_code` IN (
  'dashboard:view',
  'sku:view',
  'warehouse:view',
  'location:view',
  'supplier:view',
  'inbound:create',
  'inbound:receive',
  'inbound:qc',
  'putaway:execute',
  'outbound:view',
  'wave:create',
  'inventory:view',
  'inventory:adjust',
  'stocktake:create',
  'stocktake:execute',
  'return:view',
  'return:inspect'
);

-- 3.4 拣货员：上架、拣货、盘点执行
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT @role_picker, `id` FROM `sys_permission`
WHERE `permission_code` IN (
  'dashboard:view',
  'sku:view',
  'warehouse:view',
  'location:view',
  'putaway:execute',
  'pick:execute',
  'stocktake:execute'
);

-- 3.5 复核员：复核打包
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT @role_reviewer, `id` FROM `sys_permission`
WHERE `permission_code` IN (
  'dashboard:view',
  'sku:view',
  'review:execute'
);


-- ===================================================================
-- 四、数据字典（8 类）
-- ===================================================================
INSERT INTO `sys_dict` (`dict_type`, `dict_name`, `description`, `status`, `create_by`) VALUES
('warehouse_type',  '仓库类型',   '仓库类型枚举', 1, 'system'),
('zone_type',       '区域类型',   '仓库区域类型枚举', 1, 'system'),
('location_type',   '库位类型',   '库位类型枚举', 1, 'system'),
('location_status', '库位状态',   '库位状态枚举', 1, 'system'),
('turnover_zone',   '周转分区',   '按周转率划分的分区', 1, 'system'),
('inbound_type',    '入库类型',   '入库单据类型（R2 使用）', 1, 'system'),
('outbound_status', '出库单状态', '出库单状态流转（R3 使用）', 1, 'system'),
('stocktake_type',  '盘点类型',   '盘点任务类型（R4 使用）', 1, 'system');

INSERT INTO `sys_dict_item` (`dict_type`, `item_value`, `item_label`, `sort`) VALUES
('warehouse_type', 'CENTRAL',  '中心仓', 1),
('warehouse_type', 'REGIONAL', '区域仓', 2),
('warehouse_type', 'OVERSEAS', '海外仓', 3),
('warehouse_type', 'LOCAL',    '本地仓', 4),

('zone_type', 'STORAGE',  '存储区', 1),
('zone_type', 'RECEIVING','收货区', 2),
('zone_type', 'SHIPPING', '发货区', 3),
('zone_type', 'QC',       '质检区', 4),
('zone_type', 'RETURN',   '退货区', 5),

('location_type', 'NORMAL',         '普通库位',     1),
('location_type', 'TEMP',           '暂存库位',     2),
('location_type', 'DEFECT',         '不良品库位',   3),
('location_type', 'RETURN_PENDING', '退货待检库位', 4),

('location_status', 'FREE',        '空闲',   1),
('location_status', 'OCCUPIED',    '占用',   2),
('location_status', 'LOCKED',      '锁定',   3),
('location_status', 'MAINTENANCE', '维修中', 4),

('turnover_zone', 'FAST', '快销区', 1),
('turnover_zone', 'SLOW', '慢销区', 2),
('turnover_zone', 'DEAD', '滞销区', 3),

('inbound_type', 'PURCHASE', '采购入库', 1),
('inbound_type', 'RETURN',   '退货入库', 2),
('inbound_type', 'TRANSFER', '调拨入库', 3),

('outbound_status', 'DRAFT',     '草稿',       1),
('outbound_status', 'ALLOCATED', '已分配库存', 2),
('outbound_status', 'PICKING',   '拣货中',     3),
('outbound_status', 'PICKED',    '已拣货',     4),
('outbound_status', 'PACKING',   '打包中',     5),
('outbound_status', 'SHIPPED',   '已出库',     6),
('outbound_status', 'CANCELLED', '已取消',     7),

('stocktake_type', 'FULL',    '全盘',     1),
('stocktake_type', 'CYCLE',   '周期盘点', 2),
('stocktake_type', 'SPOT',    '抽盘',     3),
('stocktake_type', 'DYNAMIC', '动态盘点', 4);


-- ===================================================================
-- 五、编码规则（8 类单据）
-- ===================================================================
INSERT INTO `sys_code_rule` (`rule_type`, `rule_name`, `prefix`, `date_format`, `serial_length`, `serial_reset`, `current_seq`, `sample`, `status`) VALUES
('SKU',        'SKU 编码',   'SP',  NULL,       3, 'NEVER', 0, 'SP001',           1),
('INBOUND',    '入库单',     'IN',  'yyyyMMdd', 3, 'DAY',   0, 'IN20261001001',   1),
('OUTBOUND',   '出库单',     'OUT', 'yyyyMMdd', 3, 'DAY',   0, 'OUT20261001001',  1),
('WAVE',       '波次单',     'W',   'yyyyMMdd', 3, 'DAY',   0, 'W20261001001',    1),
('STOCKTAKE',  '盘点单',     'CC',  'yyyyMMdd', 3, 'DAY',   0, 'CC20261001001',   1),
('ADJUST',     '库存调整单', 'ADJ', 'yyyyMMdd', 3, 'DAY',   0, 'ADJ20261001001',  1),
('TRANSFER',   '调拨单',     'TR',  'yyyyMMdd', 3, 'DAY',   0, 'TR20261001001',   1),
('RETURN',     '退货单',     'RT',  'yyyyMMdd', 3, 'DAY',   0, 'RT20261001001',   1);


-- ===================================================================
-- 六、系统参数
-- 说明：param_value 可通过系统配置页面调整
-- ===================================================================
INSERT INTO `sys_config` (`param_key`, `param_name`, `param_value`, `default_value`, `param_group`, `need_restart`, `remark`) VALUES
('password.min.length',           '密码最小长度',       '8',    '8',    'SECURITY', 0, '新建或修改用户时密码长度下限'),
('password.require.letter.number','密码需含字母和数字', 'true', 'true', 'SECURITY', 0, '开启后密码须同时包含字母与数字'),
('login.max.fail.count',          '登录失败锁定次数',   '5',    '5',    'SECURITY', 0, '连续失败达到该次数后锁定账户'),
('login.lock.minutes',            '账户锁定分钟数',     '30',   '30',   'SECURITY', 0, '锁定时长，到期自动解锁'),
('stock.warning.enabled',         '库存预警开关',       'true', 'true', 'BUSINESS', 0, 'R2 库存预警总开关'),
('log.retention.days',            '日志保留天数',       '180',  '180',  'SYSTEM',   0, '超期操作日志/登录日志自动归档清理');


-- ===================================================================
-- 七、超级管理员
-- 密码说明：初始密码 123456
--   {noop} 前缀表示明文（Spring Security NoOpPasswordEncoder），仅用于开发/测试环境
--   生产部署前请替换为 BCrypt 密文，格式如：{bcrypt}$2a$10$xxxxxxxx...
--   生成方式：new BCryptPasswordEncoder().encode("123456")
-- ===================================================================
INSERT INTO `sys_user` (`username`, `real_name`, `password`, `role_id`, `warehouse_id`, `phone`, `email`, `status`, `force_change_password`, `create_by`)
VALUES ('admin', '系统管理员', '{noop}123456', @role_admin, NULL, '13800138000', 'admin@wms.com', 1, 1, 'system');


SET FOREIGN_KEY_CHECKS = 1;

-- ===================================================================
-- 初始化完成
--   角色 5 个 / 权限约 60 项 / 字典 8 类 / 编码规则 8 条 / 系统参数 6 项 / 管理员 1 个
--   下一步（可选）：执行 R1-V1.0.2-demo-data.sql 导入演示数据
-- ===================================================================
