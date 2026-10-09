# WMS 后端函数 UML 说明

| 项 | 内容 |
|---|---|
| 文档类型 | 函数清单 / 类图说明（UML） |
| 覆盖范围 | 已生成的后端代码（随开发持续同步） |
| 关联文档 | `需求分析/接口文档/R1-基础平台与主数据接口文档.md`<br>`设计文档/R1-后端技术设计文档.md` |
| 创建日期 | 2026-10-08 |

---

## 一、模块总览

| 模块 | 类型 | 状态 | 类数量 |
|---|---|---|---|
| `wms-common-core` | 公共 | 已完成 | 5 |
| `wms-common-web` | 公共 | 已完成 | 3 |
| `wms-common-mybatis` | 公共 | 已完成 | 5 |
| `wms-common-redis` | 公共 | 已完成 | 2 |
| `wms-common-security` | 公共 | 已完成 | 5 |
| `wms-common-log` | 公共 | 已完成 | 4 |
| `wms-api-system` | Feign 接口 | 已完成 | 7 |
| `wms-auth` | 微服务 | 已完成 | 12 |
| `wms-system` | 微服务 | 已完成 | 49 |
| `wms-base` | 微服务 | 已完成 | 44 |
| `wms-gateway` | 微服务 | 已完成 | 2 |

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
| `R<T>` | `fail(int, String)` | code, message | `R<T>` | 失败响应（自定义错误码与消息） |
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

**包**：`com.wms.common.web.handler`

| 类 | 方法 | 参数 | 返回 | 说明 |
|---|---|---|---|---|
| `GlobalExceptionHandler` | `handleBizException` | `BizException` | `R<Void>` | 业务异常 → 对应错误码响应 |
| `GlobalExceptionHandler` | `handleMethodArgumentNotValid` | `MethodArgumentNotValidException` | `R<Void>` | `@RequestBody` 参数校验失败，取首个字段错误 |
| `GlobalExceptionHandler` | `handleBindException` | `BindException` | `R<Void>` | 表单绑定校验失败 |
| `GlobalExceptionHandler` | `handleNotReadable` | `HttpMessageNotReadableException` | `R<Void>` | 请求体不可读 |
| `GlobalExceptionHandler` | `handleMethodNotSupported` | `HttpRequestMethodNotSupportedException` | `R<Void>` | 请求方法不支持（405） |
| `GlobalExceptionHandler` | `handleException` | `Exception`, `HttpServletRequest` | `R<Void>` | 兜底异常，返回 500 + traceId |
| `TraceIdFilter` | `doFilter` | Request/Response/Chain | `void` | 生成或透传 traceId，写入 MDC |
| `TraceIdFilter` | `resolveTraceId` | `ServletRequest` | `String` | 优先取 X-Trace-Id 头 |
| `ResponseAdvice` | `beforeBodyWrite` | body 等 | `Object` | 为 `R` 响应填充 timestamp 与 traceId |

**常量**：`TraceIdFilter.TRACE_ID = "traceId"`、`TRACE_HEADER = "X-Trace-Id"`

---

## 四、wms-common-mybatis

**包**：`com.wms.common.mybatis`

| 类 | 方法 | 参数 | 返回 | 说明 |
|---|---|---|---|---|
| `MybatisPlusConfig` | `mybatisPlusInterceptor()` | - | `MybatisPlusInterceptor` | 注册分页、乐观锁、防全表更新插件 |
| `AuditMetaObjectHandler` | `insertFill` | `MetaObject` | `void` | 填充 createBy/createTime/updateBy/updateTime |
| `AuditMetaObjectHandler` | `updateFill` | `MetaObject` | `void` | 填充 updateBy/updateTime |
| `AuditMetaObjectHandler` | `resolveUsername` | - | `String` | 解析当前操作人，异常兜底为 system |
| `AuditUserProvider` | `currentUsername()` | - | `String` | SPI 接口，获取当前操作人 |
| `DefaultAuditUserProvider` | `currentUsername()` | - | `String` | 默认实现，返回 `system` |
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
| `DistributedLock` | `lockAndRun(String, Runnable)` | key, task | void | 加锁执行（默认 5s 等待 / 30s 持有） |
| `DistributedLock` | `lockAndRun(String, long, long, Runnable)` | key, wait, lease, task | void | 自定义超时加锁 |
| `DistributedLock` | `lockAndGet(String, Supplier<T>)` | key, action | `T` | 加锁执行并返回结果 |
| `DistributedLock` | `lockAndGet(String, long, long, Supplier<T>)` | key, wait, lease, action | `T` | 自定义超时加锁并返回 |

---

## 六、wms-common-security

**包**：`com.wms.common.security`

```
┌─────────────────────────┐      ┌─────────────────────────────┐
│      LoginUser          │      │       UserContext (TL)      │
├─────────────────────────┤      ├─────────────────────────────┤
│ - Long userId           │      │ + set(LoginUser)            │
│ - String username       │◀────▶│ + get() : LoginUser         │
│ - String realName       │      │ + getUserId() : Long        │
│ - String roleCode       │      │ + getUsername() : String    │
│ - Long warehouseId      │      │ + getRoleCode() : String    │
│ - Set<String> perms     │      │ + getWarehouseId() : Long   │
├─────────────────────────┤      │ + clear()                   │
│ + hasPermission(String) │      └─────────────────────────────┘
└─────────────────────────┘

┌─────────────────────────────────┐
│       JwtTokenProvider          │
├─────────────────────────────────┤
│ + generateToken(LoginUser)      │
│ + buildToken(String, Map)       │
│ + parseToken(String) : Claims   │
│ + validateToken(String)         │
│ + getJti(String) : String       │
│ + getExpiration(String) : long  │
│ + getExpireSeconds() : long     │
└─────────────────────────────────┘
```

| 类 | 方法 | 参数 | 返回 | 说明 |
|---|---|---|---|---|
| `LoginUser` | `hasPermission(String)` | permissionCode | `boolean` | 判断是否拥有指定权限 |
| `UserContext` | `set/get/getUserId/getUsername/getRoleCode/getWarehouseId/clear` | - | - | ThreadLocal 用户上下文 |
| `JwtTokenProvider` | `generateToken` | `LoginUser` | `String` | 生成 JWT（含 jti、角色、仓库） |
| `JwtTokenProvider` | `buildToken` | subject, claims | `String` | 自定义声明生成 Token |
| `JwtTokenProvider` | `parseToken` | token | `Claims` | 解析 Token（失败抛 JwtException） |
| `JwtTokenProvider` | `validateToken` | token | `boolean` | 校验签名与有效期 |
| `JwtTokenProvider` | `getJti` | token | `String` | 获取 Token 唯一 ID |
| `JwtTokenProvider` | `getExpiration` | token | `long` | 获取过期时间戳 |
| `JwtTokenProvider` | `getExpireSeconds` | - | `long` | 获取有效期秒数 |
| `RequirePermission` | `value()` | - | String | 权限校验注解（方法/类） |
| `PermissionAspect` | `checkPermission` | joinPoint, annotation | `Object` | 环绕通知：无上下文抛 401，无权限抛 403 |

---

## 七、wms-common-log

**包**：`com.wms.common.log`

| 类 | 方法 | 参数 | 返回 | 说明 |
|---|---|---|---|---|
| `OperationLog` | `module()/type()/description()` | - | 注解属性 | 操作日志注解 |
| `OperationType` | 枚举：CREATE/UPDATE/DELETE/LOGIN/LOGOUT/ENABLE/DISABLE/LOCK/IMPORT/EXPORT/APPROVE/RESET/OTHER | - | - | 操作类型 |
| `OperationLogEvent` | builder 构造 | - | - | 日志事件（含脱敏参数、IP、耗时） |
| `OperationLogAspect` | `record` | joinPoint, annotation | `Object` | 环绕采集并发布事件 |
| `OperationLogAspect` | `serializeArgs` | args | `String` | 参数序列化为 JSON |
| `OperationLogAspect` | `maskSensitive` | json | `String` | password 类字段替换为 `******` |
| `OperationLogAspect` | `currentRequest` | - | `HttpServletRequest` | 从 RequestContextHolder 获取请求 |

**脱敏字段**：`password`、`oldPassword`、`newPassword`、`confirmPassword`

---

## 八、wms-api-system（Feign 接口）

**包**：`com.wms.api.system`

| 接口 | 方法 | HTTP | 路径 | 说明 |
|---|---|---|---|---|
| `UserFeignClient` | `getByUsername(String)` | GET | `/internal/users/by-username/{username}` | 查询用户（含密码密文） |
| `UserFeignClient` | `updateLoginInfo(Long, LoginInfoDTO)` | PUT | `/internal/users/{userId}/login-info` | 更新最后登录信息 |
| `UserFeignClient` | `changePassword(Long, ChangePasswordDTO)` | POST | `/internal/users/{userId}/password` | 修改密码 |
| `PermissionFeignClient` | `getUserPermissionCodes(Long)` | GET | `/internal/permissions/user/{userId}` | 查询权限标识集合 |
| `PermissionFeignClient` | `getUserMenus(Long)` | GET | `/internal/permissions/menu/{userId}` | 查询菜单树 |
| `LoginLogFeignClient` | `record(LoginLogDTO)` | POST | `/internal/logs/login` | 记录登录日志 |

**DTO**：`UserAuthDTO`、`LoginInfoDTO`、`LoginLogDTO`、`ChangePasswordDTO`、`PermissionNodeDTO`

---

## 九、wms-auth（认证服务）

**包**：`com.wms.auth`

```
┌────────────────────────────────┐
│       AuthController           │
├────────────────────────────────┤
│ + login(LoginDTO, HttpServletRequest) : R<LoginVO>       POST /api/v1/auth/login
│ + logout(Authorization) : R<Void>                        POST /api/v1/auth/logout
│ + changePassword(ChangePasswordRequest) : R<Void>        POST /api/v1/auth/password
│ + current() : R<UserInfoVO>                              GET  /api/v1/auth/current
│ + menus() : R<TokenVO>                                   GET  /api/v1/auth/menus
│ + refresh(Authorization) : R<TokenVO>                    POST /api/v1/auth/refresh
└───────────────┬────────────────┘
                │ 依赖
┌───────────────▼────────────────┐
│         AuthService            │
├────────────────────────────────┤
│ + LoginVO login(LoginDTO, String ip)
│ + void logout(String token)
│ + void changePassword(Long userId, ChangePasswordRequest)
│ + UserInfoVO currentUser(Long userId)
│ + TokenVO menus(Long userId)
│ + TokenVO refresh(String token)
└───────────────┬────────────────┘
                │ 实现
┌───────────────▼────────────────────────────────────────┐
│                AuthServiceImpl                          │
├─────────────────────────────────────────────────────────┤
│ - queryUser(String) : UserAuthDTO                       │
│ - buildLoginUser(UserAuthDTO) : LoginUser               │
│ - loadPermissions(Long) : Set<String>                   │
│ - updateLoginInfo(Long, String)                         │
│ - recordLoginLog(String, String, String, String)        │
│ - validatePassword(String)   （长度 + 字母数字校验）     │
└─────────────────────────────────────────────────────────┘
```

### 9.1 函数清单

| 类 | 方法 | 参数 | 返回 | 说明 |
|---|---|---|---|---|
| `AuthController` | `login` | `LoginDTO`, `HttpServletRequest` | `R<LoginVO>` | 登录，取客户端 IP |
| `AuthController` | `logout` | `Authorization` | `R<Void>` | 登出，Token 入黑名单 |
| `AuthController` | `changePassword` | `ChangePasswordRequest` | `R<Void>` | 修改当前用户密码 |
| `AuthController` | `current` | - | `R<UserInfoVO>` | 当前用户信息 |
| `AuthController` | `menus` | - | `R<TokenVO>` | 菜单树 + 按钮权限 |
| `AuthController` | `refresh` | `Authorization` | `R<TokenVO>` | 刷新 Token |
| `AuthServiceImpl` | `login` | dto, ip | `LoginVO` | 锁定→查用户→停用→密码→生成 Token→日志 |
| `AuthServiceImpl` | `logout` | token | `void` | jti 写入黑名单，TTL=剩余有效期 |
| `AuthServiceImpl` | `changePassword` | userId, request | `void` | 二次密码一致性 + 策略校验 + Feign 调 system |
| `AuthServiceImpl` | `currentUser` | userId | `UserInfoVO` | 从 UserContext 读取 |
| `AuthServiceImpl` | `menus` | userId | `TokenVO` | 权限集合（Redis 缓存）+ 菜单树 |
| `AuthServiceImpl` | `refresh` | token | `TokenVO` | 解析旧 Token → 生成新 Token → 旧 Token 作废 |
| `AuthServiceImpl` | `loadPermissions` | userId | `Set<String>` | 缓存优先，回源 Feign，缓存 30 分钟 |
| `AuthServiceImpl` | `validatePassword` | password | `void` | 长度 ≥8 且含字母与数字 |
| `LoginGuard` | `isLocked` | username | `boolean` | 判断是否锁定 |
| `LoginGuard` | `recordFail` | username | `int` | 失败计数，达阈值锁定，返回剩余次数 |
| `LoginGuard` | `clearFail` | username | `void` | 清除失败计数 |
| `LoginGuard` | `remainAttempts` | failCount | `int` | 计算剩余次数 |
| `UserContextInterceptor` | `preHandle` | req/resp/handler | `boolean` | 解析 JWT 填充 UserContext |
| `UserContextInterceptor` | `afterCompletion` | - | `void` | 清理 UserContext |
| `UserContextInterceptor` | `resolveToken(String)` | authorization | `String` | 静态方法：去除 Bearer 前缀 |
| `IpUtils` | `getIpAddress` | `HttpServletRequest` | `String` | 解析 X-Forwarded-For 等获取真实 IP |
| `BeanConfig` | `passwordEncoder()` | - | `PasswordEncoder` | 委派密码编码器（支持 {noop}/{bcrypt}） |
| `WebConfig` | `addInterceptors` | registry | `void` | 注册拦截器，排除 login/refresh |
| `WmsAuthApplication` | `main` | args | `void` | 启动类，开启 Feign 与服务发现 |

### 9.2 登录时序

```
Client           Gateway            AuthController        AuthService          Feign(system)      Redis
  │ POST /login      │                    │                    │                    │              │
  ├─────────────────▶│ 白名单放行          │                    │                    │              │
  │                  ├───────────────────▶│ login(dto, request) │                    │              │
  │                  │                    ├───────────────────▶│                    │              │
  │                  │                    │                    │ isLocked?          │              │
  │                  │                    │                    ├──────────────────────────────────▶│ hasKey
  │                  │                    │                    │◀──────────────────────────────────┤
  │                  │                    │                    │ getByUsername      │              │
  │                  │                    │                    ├───────────────────▶│ query sys_user│
  │                  │                    │                    │◀──────────────────┤ UserAuthDTO   │
  │                  │                    │                    │ passwordEncoder.matches           │
  │                  │                    │                    │ loadPermissions（缓存 → Feign）    │
  │                  │                    │                    │ jwtTokenProvider.generateToken    │
  │                  │                    │                    │ updateLoginInfo ──▶│              │
  │                  │                    │                    │ recordLoginLog ───▶│              │
  │                  │                    │◀──────────────────┤ LoginVO            │              │
  │◀─────────────────┤ R<LoginVO>         │                    │                    │              │
```

---

## 十、wms-system（系统管理服务）

**包**：`com.wms.system`

### 10.1 接口清单（22 个）

| 控制器 | 方法 | HTTP | 路径 | 权限 |
|---|---|---|---|---|
| `UserController` | `page(UserQuery)` | GET | `/api/v1/users` | user:view |
| `UserController` | `create(UserCreateDTO)` | POST | `/api/v1/users` | user:create |
| `UserController` | `detail(Long)` | GET | `/api/v1/users/{id}` | user:view |
| `UserController` | `update(Long, UserUpdateDTO)` | PUT | `/api/v1/users/{id}` | user:edit |
| `UserController` | `changeStatus(Long, Integer)` | PUT | `/api/v1/users/{id}/status` | user:edit |
| `UserController` | `resetPassword(Long)` | POST | `/api/v1/users/{id}/reset-password` | user:reset |
| `UserController` | `checkUsername(String)` | GET | `/api/v1/users/check-username` | user:view |
| `RoleController` | `list()` | GET | `/api/v1/roles` | role:view |
| `RoleController` | `detail(Long)` | GET | `/api/v1/roles/{id}` | role:view |
| `RoleController` | `update(Long, String, String)` | PUT | `/api/v1/roles/{id}` | role:edit |
| `RoleController` | `permissions(Long)` | GET | `/api/v1/roles/{id}/permissions` | role:view |
| `RoleController` | `updatePermissions(Long, List<String>)` | PUT | `/api/v1/roles/{id}/permissions` | role:edit |
| `PermissionController` | `tree()` | GET | `/api/v1/permissions/tree` | role:view |
| `LogController` | `operationLogs(LogQuery)` | GET | `/api/v1/logs/operation` | log:view |
| `LogController` | `loginLogs(LogQuery)` | GET | `/api/v1/logs/login` | log:view |
| `DictController` | `dictTypes(String)` | GET | `/api/v1/config/dicts` | config:view |
| `DictController` | `dictItems(String)` | GET | `/api/v1/config/dicts/items` | config:view |
| `DictController` | `refresh()` | POST | `/api/v1/config/dicts/refresh` | config:edit |
| `ConfigController` | `list(String)` | GET | `/api/v1/config/params` | config:view |
| `ConfigController` | `update(List<ConfigUpdateDTO>)` | PUT | `/api/v1/config/params` | config:edit |
| `CodeRuleController` | `list()` | GET | `/api/v1/config/code-rules` | config:view |
| `CodeRuleController` | `update(Long, ...)` | PUT | `/api/v1/config/code-rules/{id}` | config:edit |
| `CodeRuleController` | `nextCode(String)` | GET | `/api/v1/config/next-code/{ruleType}` | config:view |

### 10.2 内部接口（Feign 暴露）

| 方法 | HTTP | 路径 | 调用方 |
|---|---|---|---|
| `getByUsername(String)` | GET | `/internal/users/by-username/{username}` | wms-auth |
| `updateLoginInfo(Long, LoginInfoDTO)` | PUT | `/internal/users/{userId}/login-info` | wms-auth |
| `changePassword(Long, ChangePasswordDTO)` | POST | `/internal/users/{userId}/password` | wms-auth |
| `userPermissionCodes(Long)` | GET | `/internal/permissions/user/{userId}` | wms-auth |
| `userMenus(Long)` | GET | `/internal/permissions/menu/{userId}` | wms-auth |
| `recordLoginLog(LoginLogDTO)` | POST | `/internal/logs/login` | wms-auth |
| `nextCode(String)` | GET | `/internal/config/next-code/{ruleType}` | wms-base |

### 10.3 Service 函数

| 类 | 方法 | 说明 |
|---|---|---|
| `UserServiceImpl` | `createUser(UserCreateDTO)` | 用户名唯一校验 → BCrypt 加密初始密码 → 置强制改密 |
| `UserServiceImpl` | `updateUser / changeStatus / resetPassword` | 变更成功后清理 `auth:perms:{userId}` 缓存 |
| `UserServiceImpl` | `changePassword(Long, old, new)` | 校验原密码 → 加密新密码 → 清除强制改密标记 |
| `UserServiceImpl` | `page(UserQuery)` | 关联角色表补全 roleCode/roleName |
| `UserServiceImpl` | `updateLoginInfo` / `getByUsername` / `existsUsername` | 供认证链路使用 |
| `RoleServiceImpl` | `getPermissionTree()` | 全量权限组装树 |
| `RoleServiceImpl` | `getUserPermissionCodes(Long)` | 用户 → 角色 → 权限编码集合 |
| `RoleServiceImpl` | `getUserMenus(Long)` | 按权限过滤 MENU 类型后组装树 |
| `RoleServiceImpl` | `updateRolePermissions(Long, List<String>)` | 先删后插，清理角色下用户缓存 |
| `RoleServiceImpl` | `buildTree` / `toNode` | 私有：树组装与 DTO 转换 |
| `LogServiceImpl` | `pageOperationLogs` / `pageLoginLogs` | 条件分页查询 |
| `LogServiceImpl` | `saveOperationLog(OperationLogEvent)` | `@Async + @EventListener` 异步落库 |
| `LogServiceImpl` | `saveLoginLog(LoginLogDTO)` | 登录日志落库 |
| `DictServiceImpl` | `listDictItems(String)` | 缓存优先（60 分钟），回源查库 |
| `DictServiceImpl` | `refreshCache()` | 清理全部字典缓存 |
| `CodeRuleServiceImpl` | `generateNextCode(String)` | Redis INCR 生成流水号，失败回退 DB |
| `CodeRuleServiceImpl` | `resolveDatePart` / `resolveTtl` / `buildSample` | 私有：日期段、TTL、示例计算 |
| `ConfigServiceImpl` | `updateParams(Map)` | 批量更新并清理缓存 |
| `ConfigServiceImpl` | `getParamValue(String)` | 缓存优先读取单参数 |

---

## 十一、wms-base（基础数据服务）

**包**：`com.wms.base`

### 11.1 接口清单（30 个）

| 控制器 | 方法 | HTTP | 路径 | 权限 |
|---|---|---|---|---|
| `SkuController` | `page(SkuQuery)` | GET | `/api/v1/skus` | sku:view |
| `SkuController` | `create(SkuCreateDTO)` | POST | `/api/v1/skus` | sku:create |
| `SkuController` | `detail(Long)` | GET | `/api/v1/skus/{id}` | sku:view |
| `SkuController` | `update(Long, SkuUpdateDTO)` | PUT | `/api/v1/skus/{id}` | sku:edit |
| `SkuController` | `delete(Long)` | DELETE | `/api/v1/skus/{id}` | sku:delete |
| `SkuController` | `changeStatus(Long, Integer)` | PUT | `/api/v1/skus/{id}/status` | sku:edit |
| `SkuController` | `downloadTemplate(HttpServletResponse)` | GET | `/api/v1/skus/template` | sku:import |
| `SkuController` | `importSkus(MultipartFile)` | POST | `/api/v1/skus/import` | sku:import |
| `SkuController` | `export(HttpServletResponse, SkuQuery)` | POST | `/api/v1/skus/export` | sku:export |
| `WarehouseController` | `page(WarehouseQuery)` | GET | `/api/v1/warehouses` | warehouse:view |
| `WarehouseController` | `create(WarehouseDTO)` | POST | `/api/v1/warehouses` | warehouse:create |
| `WarehouseController` | `detail(Long)` | GET | `/api/v1/warehouses/{id}` | warehouse:view |
| `WarehouseController` | `update(Long, WarehouseDTO)` | PUT | `/api/v1/warehouses/{id}` | warehouse:edit |
| `WarehouseController` | `changeStatus(Long, Integer)` | PUT | `/api/v1/warehouses/{id}/status` | warehouse:edit |
| `WarehouseController` | `options()` | GET | `/api/v1/warehouses/options` | warehouse:view |
| `WarehouseController` | `zones(Long)` | GET | `/api/v1/warehouses/{id}/zones` | warehouse:view |
| `WarehouseController` | `createZone(Long, ZoneDTO)` | POST | `/api/v1/warehouses/{id}/zones` | warehouse:edit |
| `ZoneController` | `update(Long, ZoneDTO)` | PUT | `/api/v1/zones/{id}` | warehouse:edit |
| `LocationController` | `page(LocationQuery)` | GET | `/api/v1/locations` | location:view |
| `LocationController` | `create(LocationCreateDTO)` | POST | `/api/v1/locations` | location:create |
| `LocationController` | `batchCreate(LocationBatchDTO)` | POST | `/api/v1/locations/batch` | location:batch |
| `LocationController` | `detail(Long)` | GET | `/api/v1/locations/{id}` | location:view |
| `LocationController` | `update(Long, String, BigDecimal, BigDecimal)` | PUT | `/api/v1/locations/{id}` | location:edit |
| `LocationController` | `changeStatus(Long, String, String)` | PUT | `/api/v1/locations/{id}/status` | location:edit |
| `LocationController` | `layout(Long, Long, Integer)` | GET | `/api/v1/locations/layout` | location:view |
| `SupplierController` | `page(SupplierQuery)` | GET | `/api/v1/suppliers` | supplier:view |
| `SupplierController` | `create(SupplierDTO)` | POST | `/api/v1/suppliers` | supplier:create |
| `SupplierController` | `detail(Long)` | GET | `/api/v1/suppliers/{id}` | supplier:view |
| `SupplierController` | `update(Long, SupplierDTO)` | PUT | `/api/v1/suppliers/{id}` | supplier:edit |
| `SupplierController` | `changeStatus(Long, Integer)` | PUT | `/api/v1/suppliers/{id}/status` | supplier:edit |

### 11.2 Service 函数

| 类 | 方法 | 说明 |
|---|---|---|
| `SkuServiceImpl` | `createSku(SkuCreateDTO)` | 组合唯一 → 编码（留空走 Feign 生成）→ 条码全局唯一 → 保存 SKU 与条码 |
| `SkuServiceImpl` | `updateSku(Long, SkuUpdateDTO)` | 属性更新 + 条码全量覆盖 |
| `SkuServiceImpl` | `changeStatus / deleteSku` | 停用 / 删除（含条码级联删除） |
| `SkuServiceImpl` | `page(SkuQuery)` / `listForExport(SkuQuery)` | 分页查询 / 导出列表 |
| `SkuServiceImpl` | `importSkus(InputStream)` | EasyExcel 逐行解析 → 逐条创建 → 收集失败行与原因 |
| `SkuServiceImpl` | `parseRow(Map)` / `generateSkuCode()` / `saveBarcodes` | 私有：行解析、编码生成（Feign+兜底）、条码保存 |
| `WarehouseServiceImpl` | `createWarehouse / updateWarehouse / changeStatus` | 编码唯一校验；编码创建后不可改 |
| `WarehouseServiceImpl` | `page / listEnabled` | 分页查询 / 启用仓库下拉 |
| `WarehouseServiceImpl` | `createZone / updateZone / listZones` | 同仓库下区域编码唯一 |
| `LocationServiceImpl` | `createLocation(LocationCreateDTO)` | 生成编码 `{仓库}-{区域}-{货架}-{层}-{列}-{位}` + 唯一校验 |
| `LocationServiceImpl` | `batchCreate(LocationBatchDTO)` | 笛卡尔积生成、去重、分批插入、上限校验 |
| `LocationServiceImpl` | `changeStatus(Long, String, String)` | 占用库位不可直接置空闲 |
| `LocationServiceImpl` | `layout(Long, Long, Integer)` | 组装平面图 cells + 统计 + 颜色等级 |
| `LocationServiceImpl` | `calculateUsageRate` / `resolveColorLevel` | 私有：使用率与颜色（GREEN/YELLOW/RED/GRAY） |
| `SupplierServiceImpl` | `createSupplier / updateSupplier / changeStatus / page` | 编码唯一 + 分页 |

---

## 十二、wms-gateway（网关服务）

**包**：`com.wms.gateway`

| 类 | 方法 | 说明 |
|---|---|---|
| `WmsGatewayApplication` | `main(String[])` | 启动网关，开启服务发现 |
| `AuthGlobalFilter` | `filter(ServerWebExchange, GatewayFilterChain)` | 白名单放行 → 提取 Token → 校验签名 → 黑名单校验 → 透传用户信息 |
| `AuthGlobalFilter` | `getOrder()` | 过滤器顺序：`HIGHEST_PRECEDENCE + 100` |
| `AuthGlobalFilter` | `isWhiteList(String)` | 白名单：`/api/v1/auth/login`、`/api/v1/auth/refresh` |
| `AuthGlobalFilter` | `resolveToken(String)` | 去除 `Bearer ` 前缀 |
| `AuthGlobalFilter` | `unauthorized(ServerWebExchange, String)` | 返回 401 JSON 响应 |

**透传请求头**：`X-User-Id`、`X-User-Name`、`X-Role-Code`、`X-Warehouse-Id`

**路由配置**（`application.yml`）：

| 路由 ID | 路径前缀 | 目标服务 |
|---|---|---|
| wms-auth | `/api/v1/auth/**` | `lb://wms-auth` |
| wms-system | `/api/v1/users/**`、`/roles/**`、`/permissions/**`、`/logs/**`、`/config/**` | `lb://wms-system` |
| wms-base | `/api/v1/skus/**`、`/warehouses/**`、`/zones/**`、`/locations/**`、`/suppliers/**` | `lb://wms-base` |

---

## 十三、附录

### 10.1 错误码对照（已实现部分）

| 错误码 | 常量 | 触发位置 |
|---|---|---|
| 10001 | USER_CREDENTIAL_ERROR | `AuthServiceImpl#login`（用户不存在 / 密码错误） |
| 10002 | USER_DISABLED | `AuthServiceImpl#login` |
| 10003 | USER_LOCKED | `AuthServiceImpl#login`（LoginGuard 命中） |
| 10006 | PASSWORD_TOO_SHORT | `AuthServiceImpl#validatePassword` |
| 10007 | PASSWORD_NEED_LETTER_NUMBER | `AuthServiceImpl#validatePassword` |
| 401 | UNAUTHORIZED | `AuthController`（无上下文）、`PermissionAspect` |
| 403 | FORBIDDEN | `PermissionAspect`（权限不足） |
| 400 | PARAM_ERROR | `GlobalExceptionHandler`（参数校验） |
| 500 | SYSTEM_ERROR | `GlobalExceptionHandler`（兜底） |

> 本文件随代码生成持续同步；新增服务后追加对应章节。
