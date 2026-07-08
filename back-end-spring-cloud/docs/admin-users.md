# 后台个人用户管理

## 定位

后台个人用户管理由 `auth-center` 提供接口，直接管理身份库 `cot_identity` 中的账号数据：

- `iam_user`：账号主体，包含 `username`、`password_hash`、`display_name`、联系方式、用户类型和状态。
- `iam_user_role`：用户与角色的关系，新用户默认绑定 `USER`。
- `iam_permission`：权限字典表，不是用户注册结果表，普通注册不会往这里插入数据。

## 注册字段说明

用户端注册页面字段与数据库映射：

| 页面字段 | 请求字段 | 数据库字段 | 说明 |
| --- | --- | --- | --- |
| 姓名 | `name` / `displayName` | `iam_user.display_name` | 优先使用姓名，没传时才回退到用户名 |
| 用户名 | `username` | `iam_user.username` | 登录账号，唯一 |
| 密码 | `password` | `iam_user.password_hash` | 服务端使用 BCrypt 加密后写入，数据库不会保存明文密码 |

因此，输入姓名 `1`、用户名 `2`、密码 `3` 时，正确结果应为：

- `username = 2`
- `display_name = 1`
- `password_hash = BCrypt(3)`

## 后台接口

网关路径前缀为 `/api`，由 `gateway` 转发到 `auth-center`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/admin/users?keyword=&status=&userType=&page=&pageSize=` | 分页查询账号 |
| `POST` | `/api/admin/users` | 新增账号，并绑定单个角色 |
| `PUT` | `/api/admin/users/{id}` | 更新账号资料、状态、用户类型和角色 |
| `PATCH` | `/api/admin/users/{id}/status` | 修改账号状态 |
| `PATCH` | `/api/admin/users/{id}/password` | 重置密码 |
| `DELETE` | `/api/admin/users/{id}` | 删除账号 |

后台接口仅允许 `SUPER_ADMIN` 和 `ADMIN` 访问，权限来自网关注入的 `X-User-Roles`。接口返回 `UserVO`，不会返回 `password_hash`。

## 前端页面

- 管理员端入口：系统管理 / 个人用户
- 页面文件：`cot-admin-web/src/views/system/UserManage.vue`
- API 封装：`cot-admin-web/src/api/adminUsers.js`
- 管理员端登录：`cot-admin-web/src/stores/user.js` 调用 `/api/auth/login`，不再使用 mock token。
