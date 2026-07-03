# auth-center

## 定位

认证中心，负责用户注册、登录、Token 签发、Token 校验和基础用户账号管理。

## 数据库

- `cot_identity`
- 主要表：`iam_user`

## 前端对接

后台管理系统登录后应保存 JWT，并在请求头中携带：

```http
Authorization: Bearer <token>
```

## 注意事项

网关配置指向 `http://localhost:8080`，本地联调时建议显式配置 `server.port: 8080`。