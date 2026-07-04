# auth-center

## 定位

`auth-center` 是认证中心，负责用户注册、登录、刷新 Token、登出、Token 校验和基础账号资料。它是前台和后台进入系统的认证入口。

## 端口和数据库

网关当前指向：

```text
http://localhost:8080
```

配置文件中未显式声明 `server.port` 时，Spring Boot 默认端口为 `8080`。

数据库：

```yaml
cot_identity
```

主要表：

- `iam_user`
- 角色、权限、刷新 Token、登录审计等表以 SQL 脚本为准

## 关键目录

```text
auth-center
├─ src/main/java/org/example/authcenter
│  ├─ controller
│  │  ├─ AuthController.java
│  │  └─ UserController.java
│  ├─ entity
│  ├─ mapper
│  ├─ service
│  └─ utils
└─ src/main/resources
   ├─ application.yml
   └─ mapper
```

## 主要接口

基础路径：

```text
/api/auth
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/auth/login` | 登录，返回 access token 和 refresh token |
| `POST` | `/api/auth/register` | 注册账号 |
| `POST` | `/api/auth/refresh` | 使用 refresh token 刷新 access token |
| `POST` | `/api/auth/logout` | 登出 |
| `GET` | `/api/auth/verify` | 校验当前 Token |

用户基础资料路径：

```text
/api/user
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/user/getUserById` | 获取当前/指定用户信息 |
| `GET` | `/api/user/public/{id}` | 获取公开用户信息 |
| `POST` | `/api/user/uploadAvatar` | 上传头像 |
| `PUT` | `/api/user/updateUserInfo` | 更新用户基础资料 |

## Token 约定

请求头：

```http
Authorization: Bearer <token>
```

配置项：

```yaml
jwt:
  secret: mySecretKeyForJWTGenerationWithEnoughLength12345678
  expiration: 3600000
  refreshExpiration: 604800000
```

生产环境必须覆盖 `jwt.secret`，不要使用仓库默认值。

## 前端对接

用户端：

- 登录页：`front-end/src/views/auth/login.vue`
- Token 工具：`front-end/src/utils/token.js`
- 请求封装：`front-end/src/utils/request.js`

后台端：

- 登录页：`cot-admin-web/src/views/LoginView.vue`
- Token 工具：`cot-admin-web/src/utils/auth.js`
- 用户 Store：`cot-admin-web/src/stores/user.js`

## 运行注意事项

- 本地联调时建议显式设置 `server.port: 8080`，避免与其他 Spring Boot 服务冲突。
- Redis 配置在 `application.yml` 中，若启用刷新 Token、黑名单或在线状态能力，需要保证 Redis 可用。
- 修改用户返回字段时，要同步检查前台、后台读取用户信息的字段名。
