# WMS 后端函数 UML 说明

| 项 | 内容 |
|---|---|
| 文档类型 | 函数清单 / 类图说明（UML） |
| 覆盖范围 | 已生成的后端代码（随开发持续同步） |
| 关联文档 | `需求分析/接口文档/R1-基础平台与主数据接口文档.md`<br>`设计文档/R1-后端技术设计文档.md`<br>`测试场景/R1-基础平台与主数据测试场景与测试点.md` |
| 创建日期 | 2026-10-08 |
| 更新日期 | 2026-10-09 |

---

## 一、模块总览

| 模块 | 类型 | 状态 | 类数量 | 测试 |
|---|---|---|---|---|
| `wms-common-core` | 公共 | 已完成 | 5 | - |
| `wms-common-web` | 公共 | 已完成 | 4 | - |
| `wms-common-mybatis` | 公共 | 已完成 | 5 | - |
| `wms-common-redis` | 公共 | 已完成 | 2 | - |
| `wms-common-security` | 公共 | 已完成 | 5 | - |
| `wms-common-log` | 公共 | 已完成 | 4 | - |
| `wms-api-system` | Feign 接口 | 已完成 | 9 | - |
| `wms-auth` | 微服务 | 已完成 | 12 | 21 |
| `wms-system` | 微服务 | 已完成 | 49 | 26 |
| `wms-base` | 微服务 | 已完成 | 44 | 31 |
| `wms-gateway` | 微服务 | 已完成 | 2 | - |

---

## 二、wms-common-core

**包**：`com.wms.common.core`

```
┌──────────────────────────────────────────────────────┐
│                    R<T>                               │
├──────────────────────────────────────────────────────┤
│ - int code                                            │
│ - String message                                      │
│ - T data                                              │
│ - long timestamp                                      │
│ - String traceId                                      │
├──────────────────────────────────────────────────────┤
│ + R<T> ok()                                           │
│ + R<T> ok(T data)                                     │
│ + R<T> fail(ErrorCode)                                │
│ + R<T> fail(int code, String message)                 │
│ + boolean isSuccess()                                 │
└──────────────────────────────────────────────────────┘
                    ▲ 使用
                    │
┌───────────────────────────────┐   ┌──────────────────────────┐
│        ErrorCode (enum)       │   │  BizException extends    │
├───────────────────────────────┤   │  RuntimeException        │
│ + int code                    │   ├──────────────────────────┤
│ + String message              │   │ - int code               │
│ SUCCESS(0)                    │   │ + BizException(ErrorCode)│
│ PARAM_ERROR(400)              │   │ + BizException(int,String)│
│ UNAUTHORIZED(401)             │   │ + int getCode()          │
│ FORBIDDEN(403)                │   └──────────────────────────┘
│ NOT_FOUND(404)                │
│ SYSTEM_ERROR(500)             │
│ 1xxxx 用户认证 / 2xxxx SKU     │
│ 3xxxx 仓库 / 4xxxx 库位        │
│ 5xxxx 供应商 / 6xxxx 角色      │
│ 7xxxx 配置                     │
└───────────────────────────────┘
```

### 2.1 函数清单

| 类 | 方法 | 参数 | 返回 | 说明 |
|---|---|---|---|---|
| `R<T>` | `ok()` | - | `R<T>` | 成功响应（无数据） |
| `R<T>` | `ok(T data)` | data | `R<T>` | 成功响应（带数据） |
| `R<T>` | `fail(ErrorCode)` | errorCode | `R<T>` | 失败响应（枚举错误码） |
| `R<T>` | `fail(int, String)` | code, message | `R<T>` | 失败响应（自定义） |
| `R<T>` | `isSuccess()` | - | `boolean` | 判断是否成功 |
| `ErrorCode` | `getCode()` / `getMessage()` | - | int / String | 错误码与消息 |
| `BizException` | `getCode()` | - | `int` | 获取业务错误码 |

### 2.2 分页对象

| 类 | 方法/字段 | 说明 |
|---|---|---|
| `PageQuery` | `pageNum`(默认1)、`pageSize`(默认20，上限100) | 分页入参基类 |
| `PageResult<T>` | `of(List<T>, long total, long pageNum, long pageSize)` | 构造分页结果，自动计算 pages |
| `PageResult<T>` | `empty()` | 构造空分页结果 |

---

## 三、wms-common-web

**包**：`com.wms.common.web`

| 类 | 方法 | 参数 | 返回 | 说明 |
|---|---|---|---|---|
| `GlobalExceptionHandler` | `handleBizException` | `BizException` | `R<Void>` | 业务异常 → 对应错误码 |
| `GlobalExceptionHandler` | `handleMethodArgumentNotValid` | `MethodArgumentNotValidException` | `R<Void>` | `@RequestBody` 校验失败，取首个字段错误 |
| `GlobalExceptionHandler` | `handleBindException` | `BindException` | `R<Void>` | 表单绑定校验失败 |
| `GlobalExceptionHandler` | `handleNotReadable` | `HttpMessageNotReadableException` | `R<Void>` | 请求体不可读 |
| `GlobalExceptionHandler` | `handleMethodNotSupported` | `HttpRequestMethodNotSupportedException` | `R<Void>` | 405 |
| `GlobalExceptionHandler` | `handleException` | `Exception`, `HttpServletRequest` | `R<Void>` | 兜底，返回 500 + traceId |
| `TraceIdFilter` | `doFilter` | Request/Response/Chain | `void` | 生成/透传 traceId 写入 MDC |
| `TraceIdFilter` | `resolveTraceId` | `ServletRequest` | `String` | 优先取 X-Trace-Id |
| `ResponseAdvice` | `beforeBodyWrite` | body 等 | `Object` | 为 `R` 填充 timestamp 与 traceId |
| `TokenAuthInterceptor` | `preHandle` | req/resp/handler | `boolean` | 解析 JWT 填充 UserContext（通用） |
| `TokenAuthInterceptor` | `afterCompletion` | - | `void` | 清理 UserContext |
| `TokenAuthInterceptor` | `resolveToken(String)` | authorization | `String` | 静态方法：去除 Bearer 前缀 |

**常量**：`TraceIdFilter.TRACE_ID = "traceId"`、`TRACE_HEADER = "X-Trace-Id"`

---

## 四、wms-common-mybatis

**包**：`com.wms.common.mybatis`

| 类 | 方法 | 参数 | 返回 | 说明 |
|---|---|---|---|---|
| `MybatisPlusConfig` | `mybatisPlusInterceptor()` | - | `MybatisPlusInterceptor` | 注册分页、乐观锁、防全表更新插件 |
| `AuditMetaObjectHandler` | `insertFill` | `MetaObject` | `void` | 填充 createBy/createTime/updateBy/updateTime |
| `AuditMetaObjectHandler` | `updateFill` | `MetaObject` | `void` | 填充 updateBy/updateTime |
| `AuditMetaObjectHandler` | `resolveUsername` | - | `String` | 解析操作人，异常兜底 system |
| `AuditUserProvider` | `currentUsername()` | - | `String` | SPI 接口 |
| `DefaultAuditUserProvider` | `currentUsername()` | - | `String` | 默认实现返回 `system` |
| `PageConvert` | `toResult(IPage<T>)` | page | `PageResult<T>` | MP 分页转统一分页结果 |

---

## 五、wms-common-redis

**包**：`com.wms.common.redis`

| 类 | 方法 | 参数 | 返回 | 说明 |
|---|---|---|---|---|
| `RedisKeys` | `userPerms(Long)` | userId | String | `wms:auth:perms:{userId}` |
| `RedisKeys` | `tokenBlacklist(String)` | jti | String | `wms:auth:token:blacklist:{jti}` |
| `RedisKeys` | `loginFail(String)` | username | String | `wms:auth:login:fail:{username}` |
| `RedisKeys` | `loginLock(String)` | username | String | `wms:auth:login:lock:{username}` |
| `RedisKeys` | `dict(String)` | dictType | String | `wms:sys:dict:{dictType}` |
| `RedisKeys` | `config(String)` | paramKey | String | `wms:sys:config:{paramKey}` |
| `RedisKeys` | `seq(String, String)` | ruleType, date | String | `wms:seq:{ruleType}:{date}` |
| `RedisKeys` | `lock(String)` | bizKey | String | `wms:lock:{bizKey}` |
| `RedisKeys` | `idempotent(String)` | key | String | `wms:idempotent:{key}` |
| `DistributedLock` | `lockAndRun(String, Runnable)` | key, task | void | 加锁执行（5s 等待 / 30s 持有） |
| `DistributedLock` | `lockAndGet(String, Supplier<T>)` | key, action | `T` | 加锁执行并返回 |

---

## 六、wms-common-security

**包**：`com.wms.common.security`

| 类 | 方法 | 参数 | 返回 | 说明 |
|---|---|---|---|---|
| `LoginUser` | `hasPermission(String)` | permissionCode | `boolean` | 判断是否拥有权限 |
| `UserContext` | `set/get/getUserId/getUsername/getRoleCode/getWarehouseId/clear` | - | - | ThreadLocal 上下文 |
| `JwtTokenProvider` | `generateToken` | `LoginUser` | `String` | 生成 JWT（含 jti/角色/仓库） |
| `JwtTokenProvider` | `buildToken` | subject, claims | `String` | 自定义声明生成 |
| `JwtTokenProvider` | `parseToken` | token | `Claims` | 解析（失败抛 JwtException） |
| `JwtTokenProvider` | `validateToken` | token | `boolean` | 校验签名与有效期 |
| `JwtTokenProvider` | `getJti` / `getExpiration` / `getExpireSeconds` | - | String / long / long | Token 元信息 |
| `RequirePermission` | `value()` | - | String | 权限注解（方法/类） |
| `PermissionAspect` | `checkPermission` | joinPoint, annotation | `Object` | 无上下文 401，无权限 403 |

---

## 七、wms-common-log

**包**：`com.wms.common.log`

| 类 | 方法 | 参数 | 返回 | 说明 |
|---|---|---|---|---|
| `OperationLog` | `module()/type()/description()` | - | 注解属性 | 操作日志注解 |
| `OperationType` | 枚举 13 项 | - | - | CREATE/UPDATE/DELETE/LOGIN/... |
| `OperationLogEvent` | builder 构造 | - | - | 日志事件（脱敏参数、IP、耗时） |
| `OperationLogAspect` | `record` | joinPoint, annotation | `Object` | 环绕采集并发布事件 |
| `OperationLogAspect` | `serializeArgs` | args | `String` | 参数序列化 |
| `OperationLogAspect` | `maskSensitive` | json | `String` | password 类字段 → `******` |

**脱敏字段**：`password`、`oldPassword`、`newPassword`、`confirmPassword`

---

## 八、wms-api-system（Feign 接口）

**包**：`com.wms.api.system`

| 接口 | 方法 | HTTP | 路径 |
|---|---|---|---|
| `UserFeignClient` | `getByUsername(String)` | GET | `/internal/users/by-username/{username}` |
| `UserFeignClient` | `updateLoginInfo(Long, LoginInfoDTO)` | PUT | `/internal/users/{userId}/login-info` |
| `UserFeignClient` | `changePassword(Long, ChangePasswordDTO)` | POST | `/internal/users/{userId}/password` |
| `PermissionFeignClient` | `getUserPermissionCodes(Long)` | GET | `/internal/permissions/user/{userId}` |
| `PermissionFeignClient` | `getUserMenus(Long)` | GET | `/internal/permissions/menu/{userId}` |
| `LoginLogFeignClient` | `record(LoginLogDTO)` | POST | `/internal/logs/login` |
| `CodeRuleFeignClient` | `nextCode(String)` | GET | `/internal/config/next-code/{ruleType}` |

**DTO**：`UserAuthDTO`、`LoginInfoDTO`、`LoginLogDTO`、`ChangePasswordDTO`、`PermissionNodeDTO`

---

## 九、wms-auth（认证服务）

**包**：`com.wms.auth`

| 类 | 方法 | HTTP | 路径 | 权限 |
|---|---|---|---|---|
| `AuthController` | `login(LoginDTO, HttpServletRequest)` | POST | `/api/v1/auth/login` | 白名单 |
| `AuthController` | `logout(Authorization)` | POST | `/api/v1/auth/logout` | 需认证 |
| `AuthController` | `changePassword(ChangePasswordRequest)` | POST | `/api/v1/auth/password` | 需认证 |
| `AuthController` | `current()` | GET | `/api/v1/auth/current` | 需认证 |
| `AuthController` | `menus()` | GET | `/api/v1/auth/menus` | 需认证 |
| `AuthController` | `refresh(Authorization)` | POST | `/api/v1/auth/refresh` | 白名单 |

| 类 | 方法 | 说明 |
|---|---|---|
| `AuthServiceImpl` | `login` | 锁定→查用户→停用→密码→Token→日志 |
| `AuthServiceImpl` | `logout` | jti 写入黑名单，TTL = 剩余有效期 |
| `AuthServiceImpl` | `changePassword` | 一致性 + 策略校验 + Feign 调 system |
| `AuthServiceImpl` | `currentUser` | 从 UserContext 读取 |
| `AuthServiceImpl` | `menus` | 权限（Redis 缓存）+ 菜单树 |
| `AuthServiceImpl` | `refresh` | 生成新 Token，旧 Token 作废 |
| `AuthServiceImpl` | `loadPermissions` / `validatePassword` | 私有：权限加载、密码策略 |
| `LoginGuard` | `isLocked/recordFail/clearFail/remainAttempts` | Redis 计数与锁定 |
| `IpUtils` | `getIpAddress` | 解析 X-Forwarded-For |
| `BeanConfig` | `passwordEncoder()` | 委派密码编码器 |
| `WebConfig` | `addInterceptors` | 排除 login/refresh |

---

## 十、wms-system（系统管理服务）

**包**：`com.wms.system`

### 10.1 对外接口（22 个）

| 控制器 | 方法 | HTTP | 路径 | 权限 |
|---|---|---|---|---|
| `UserController` | `page` | GET | `/api/v1/users` | user:view |
| `UserController` | `create` | POST | `/api/v1/users` | user:create |
| `UserController` | `detail` | GET | `/api/v1/users/{id}` | user:view |
| `UserController` | `update` | PUT | `/api/v1/users/{id}` | user:edit |
| `UserController` | `changeStatus` | PUT | `/api/v1/users/{id}/status` | user:edit |
| `UserController` | `resetPassword` | POST | `/api/v1/users/{id}/reset-password` | user:reset |
| `UserController` | `checkUsername` | GET | `/api/v1/users/check-username` | user:view |
| `RoleController` | `list/detail/update/permissions/updatePermissions` | - | `/api/v1/roles/**` | role:view / role:edit |
| `PermissionController` | `tree` | GET | `/api/v1/permissions/tree` | role:view |
| `LogController` | `operationLogs/loginLogs` | GET | `/api/v1/logs/operation`、`/login` | log:view |
| `DictController` | `dictTypes/dictItems/refresh` | - | `/api/v1/config/dicts/**` | config:view / edit |
| `ConfigController` | `list/update` | - | `/api/v1/config/params` | config:view / edit |
| `CodeRuleController` | `list/update/nextCode` | - | `/api/v1/config/code-rules`、`/next-code` | config:view / edit |

### 10.2 内部接口（Feign 暴露 7 个）

`getByUsername` / `updateLoginInfo` / `changePassword` / `userPermissionCodes` / `userMenus` / `recordLoginLog` / `nextCode`

### 10.3 Service 函数

| 类 | 方法 | 说明 |
|---|---|---|
| `UserServiceImpl` | `createUser` | 用户名唯一 + BCrypt 初始密码 + 强制改密 |
| `UserServiceImpl` | `updateUser/changeStatus/resetPassword/changePassword` | 变更后清理权限缓存 |
| `UserServiceImpl` | `getUserDetail(Long)` | 返回 VO（内部用 `super.getById`） |
| `UserServiceImpl` | `page` | 关联角色补全 roleCode/roleName |
| `RoleServiceImpl` | `getPermissionTree/getUserPermissionCodes/getUserMenus` | 权限树与用户权限 |
| `RoleServiceImpl` | `updateRolePermissions` | 先删后插 + 清理角色用户缓存 |
| `LogServiceImpl` | `saveOperationLog` | `@Async + @EventListener` 异步落库 |
| `DictServiceImpl` | `listDictItems/refreshCache` | 缓存优先（60 分钟） |
| `CodeRuleServiceImpl` | `generateNextCode` | Redis INCR + DB 兜底 |
| `ConfigServiceImpl` | `updateParams/getParamValue` | 批量更新与缓存读取 |

---

## 十一、wms-base（基础数据服务）

**包**：`com.wms.base`

### 11.1 接口清单（30 个）

| 控制器 | 方法 | HTTP | 路径 | 权限 |
|---|---|---|---|---|
| `SkuController` | `page/create/detail/update/delete/changeStatus` | - | `/api/v1/skus/**` | sku:view/create/edit/delete |
| `SkuController` | `downloadTemplate/importSkus/export` | - | `/api/v1/skus/template`、`/import`、`/export` | sku:import/export |
| `WarehouseController` | `page/create/detail/update/changeStatus/options` | - | `/api/v1/warehouses/**` | warehouse:view/create/edit |
| `WarehouseController` | `zones/createZone` | - | `/api/v1/warehouses/{id}/zones` | warehouse:view/edit |
| `ZoneController` | `update` | PUT | `/api/v1/zones/{id}` | warehouse:edit |
| `LocationController` | `page/create/batchCreate/detail/update/changeStatus/layout` | - | `/api/v1/locations/**` | location:view/create/batch/edit |
| `SupplierController` | `page/create/detail/update/changeStatus` | - | `/api/v1/suppliers/**` | supplier:view/create/edit |

### 11.2 Service 函数

| 类 | 方法 | 说明 |
|---|---|---|
| `SkuServiceImpl` | `createSku` | 组合唯一 → 编码（Feign/兜底）→ 条码唯一 → 保存 |
| `SkuServiceImpl` | `updateSku/changeStatus/deleteSku` | 条码全量覆盖 / 级联删除 |
| `SkuServiceImpl` | `importSkus(InputStream)` | EasyExcel 逐行解析，收集失败行 |
| `SkuServiceImpl` | `getSkuDetail(Long)` | 返回 VO（内部 super.getById） |
| `WarehouseServiceImpl` | `createWarehouse/updateWarehouse/changeStatus/listEnabled` | 编码唯一，编码不可改 |
| `WarehouseServiceImpl` | `createZone/updateZone/listZones` | 同仓库下区域编码唯一 |
| `LocationServiceImpl` | `createLocation` | 生成 `{仓库}-{区域}-{货架}-{层}-{列}-{位}` |
| `LocationServiceImpl` | `batchCreate` | 笛卡尔积 + 去重 + 分批 + 上限校验 |
| `LocationServiceImpl` | `changeStatus` | 占用不可直接置空闲 |
| `LocationServiceImpl` | `layout` | 颜色等级 GREEN/YELLOW/RED/GRAY + 统计 |
| `SupplierServiceImpl` | `createSupplier/updateSupplier/changeStatus/page` | 编码唯一 + 分页 |

---

## 十二、wms-gateway（网关服务）

**包**：`com.wms.gateway`

| 类 | 方法 | 说明 |
|---|---|---|
| `AuthGlobalFilter` | `filter` | 白名单 → 提取 Token → 校验 → 黑名单 → 透传用户信息 |
| `AuthGlobalFilter` | `getOrder()` | `HIGHEST_PRECEDENCE + 100` |
| `AuthGlobalFilter` | `isWhiteList` / `resolveToken` / `unauthorized` | 私有辅助 |

**透传头**：`X-User-Id`、`X-User-Name`、`X-Role-Code`、`X-Warehouse-Id`

**路由**：`/api/v1/auth/**` → wms-auth；`/users|roles|permissions|logs|config/**` → wms-system；`/skus|warehouses|zones|locations|suppliers/**` → wms-base

---

## 十三、单元测试用例

位于各微服务 `src/test/java`，JUnit 5 + Mockito，`mvn test` 全部通过。

| 服务 | 测试类 | 用例数 | 覆盖测试点 |
|---|---|---|---|
| wms-auth | `LoginGuardTest` | 7 | TP-R1-1.1.1-04 ~ 07 |
| wms-auth | `AuthServiceImplTest` | 14 | TP-R1-1.1.1-01 ~ 10、菜单、刷新 |
| wms-system | `UserServiceImplTest` | 11 | TP-R1-1.2.1-01 ~ 13 |
| wms-system | `RoleServiceImplTest` | 7 | TP-R1-1.2.2 ~ 1.2.3 |
| wms-system | `CodeRuleServiceImplTest` | 8 | TP-R1-1.7.2-02 ~ 05 |
| wms-base | `SkuServiceImplTest` | 11 | TP-R1-1.3.1 ~ 1.3.2 |
| wms-base | `LocationServiceImplTest` | 9 | TP-R1-1.5.1 / 1.5.2 / 1.5.4 / 1.5.5 |
| wms-base | `WarehouseServiceImplTest` | 11 | TP-R1-1.4.1 ~ 1.4.3 |
| **合计** | **8 个测试类** | **78** | **全部通过** |

### 13.1 关键用例断言

| 用例 | 断言 |
|---|---|
| 登录成功 | 返回 Token、清除失败计数、记录登录日志 |
| 账户锁定 / 停用 | 10003 / 10002，锁定时不查用户 |
| 密码错误 | 10001，消息含"剩余 N 次" |
| 改密校验 | 不一致 400、过短 10006、缺字母数字 10007、原密码错 10005 |
| SKU 组合唯一 | 提示"组合已存在" |
| SKU 条码唯一 | 20002 |
| SKU 编码兜底 | Feign 不可用时使用本地生成 |
| SKU 导入 | 解析 Excel 逐条创建，统计成功/失败 |
| 库位编码 | `WH-001-A-01-01-01-01` |
| 库位批量 | 超限 40004、规格 0 时 400 |
| 库位状态 | 占用不可直接置空闲 40005 |
| 平面图 | 颜色 GREEN/RED/GRAY + 统计数量 |
| 编码规则 | 前缀+日期+补零流水、Redis 失败回退 DB |

### 13.2 测试发现并修复的生产缺陷

| 缺陷 | 影响 | 修复 |
|---|---|---|
| `getById(Long)` 覆写 `return this.getById(id)` | 调用即 StackOverflowError | 改 `super.getById(id)`（Role/Warehouse/Supplier/Location） |
| `UserService.getById` 与 `ServiceImpl.getById` 返回类型冲突 | 编译期类型错误 | 改名 `getUserDetail` / `getSkuDetail`，内部 super |
| `lambdaQuery()` 依赖 MyBatis 全局配置 | 单测不可用 | 改 `LambdaQueryWrapper` / `Wrappers.lambdaQuery()` |
| SKU 导入模板表头写反（1 列 13 行） | 模板列错误 | 修正为 13 列单层表头 |
| `removeById` 依赖 TableInfo | 单测不可用 | 改 `baseMapper.deleteById` |

---

## 十四、附录

### 14.1 错误码对照

| 错误码 | 常量 | 触发位置 |
|---|---|---|
| 10001 | USER_CREDENTIAL_ERROR | `AuthServiceImpl#login` |
| 10002 | USER_DISABLED | `AuthServiceImpl#login` |
| 10003 | USER_LOCKED | `AuthServiceImpl#login`（LoginGuard） |
| 10004 | USERNAME_EXISTS | `UserServiceImpl#createUser` |
| 10005 | OLD_PASSWORD_ERROR | `UserServiceImpl#changePassword` |
| 10006 / 10007 | 密码策略 | `AuthServiceImpl#validatePassword` |
| 20001 / 20002 | SKU 编码 / 条码冲突 | `SkuServiceImpl#createSku` |
| 30001 / 30003 / 30004 | 仓库 / 区域 | `WarehouseServiceImpl` |
| 40001 / 40004 / 40005 | 库位编码 / 批量 / 状态 | `LocationServiceImpl` |
| 50001 | SUPPLIER_CODE_EXISTS | `SupplierServiceImpl` |
| 60002 | ROLE_NOT_FOUND | `RoleServiceImpl` |
| 70001 | CODE_RULE_NOT_FOUND | `CodeRuleServiceImpl` |
| 401 / 403 | UNAUTHORIZED / FORBIDDEN | `AuthController`、`PermissionAspect` |
| 400 / 500 | PARAM_ERROR / SYSTEM_ERROR | `GlobalExceptionHandler` |

### 14.2 构建与测试命令

```bash
# 构建公共模块与 API
cd wms-common && mvn clean install -DskipTests
cd ../wms-api   && mvn clean install -DskipTests

# 单服务测试
cd wms-auth   && mvn test
cd wms-system && mvn test
cd wms-base   && mvn test

# 全部构建
for p in wms-auth wms-system wms-base wms-gateway; do (cd $p && mvn clean install -DskipTests); done
```

### 14.3 测试环境注意

- 本机 JDK 25：Lombok ≥ 1.18.48、Mockito 5.24、byte-buddy 1.18.14
- Mockito 使用 `mock-maker-subclass`（`src/test/resources/mockito-extensions/`）
- ServiceImpl 的 `baseMapper` 需在测试中通过 `ReflectionTestUtils` 注入

> 本文件随代码生成持续同步。
