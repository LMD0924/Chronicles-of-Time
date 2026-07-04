# user-center

## 定位

`user-center` 是用户扩展资料和简历服务。认证中心只负责账号和登录态，用户中心负责更完整的个人资料、人生阶段资料和简历信息。

## 端口和数据库

```yaml
server:
  port: 8081
```

数据库：

```yaml
cot_profile
```

注意：当前配置里的 `spring.application.name` 写成了 `auth-center`，实际模块是 `user-center`。若后续接入注册中心或服务发现，应改为 `user-center`。

## 关键目录

```text
user-center
├─ src/main/java/org/example/usercenter
│  ├─ controller
│  │  ├─ UserInfoController.java
│  │  └─ ResumeController.java
│  ├─ entity
│  ├─ mapper
│  └─ service
└─ src/main/resources
   ├─ application.yml
   └─ mapper
```

## 主要对象

- `UserInfo`：用户扩展资料。
- `Resume`：简历主体。
- `EducationExperience`：教育经历。
- `ProjectExperience`：项目经历。
- `WorkExperience`：工作经历。
- `SocialExperience`：社会实践。
- `Skill`：技能。
- `Certificate`：证书。

## 主要接口

用户资料：

```text
/api/userInfo
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/userInfo/add` | 新增用户扩展资料 |
| `PUT` | `/api/userInfo/update` | 更新用户扩展资料 |
| `GET` | `/api/userInfo/get/{id}` | 按 ID 获取资料 |
| `GET` | `/api/userInfo/getCurrent` | 获取当前登录用户资料 |
| `GET` | `/api/userInfo/list` | 查询资料列表 |
| `DELETE` | `/api/userInfo/delete/{id}` | 删除资料 |

简历：

```text
/api/resume
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/resume/addResume` | 新增简历主体 |
| `GET` | `/api/resume/getCompleteResume` | 获取完整简历 |
| `POST` | `/api/resume/addEducation` | 新增教育经历 |
| `POST` | `/api/resume/updateEducation` | 更新教育经历 |
| `POST` | `/api/resume/deleteEducation` | 删除教育经历 |
| `POST` | `/api/resume/addProject` | 新增项目经历 |
| `POST` | `/api/resume/updateProject` | 更新项目经历 |
| `POST` | `/api/resume/deleteProject` | 删除项目经历 |
| `POST` | `/api/resume/addWorkExperience` | 新增工作经历 |
| `POST` | `/api/resume/updateWorkExperience` | 更新工作经历 |
| `POST` | `/api/resume/deleteWorkExperience` | 删除工作经历 |
| `POST` | `/api/resume/addSkill` | 新增技能 |
| `POST` | `/api/resume/updateSkill` | 更新技能 |
| `POST` | `/api/resume/deleteSkill` | 删除技能 |
| `POST` | `/api/resume/addCertificate` | 新增证书 |
| `POST` | `/api/resume/updateCertificate` | 更新证书 |
| `POST` | `/api/resume/deleteCertificate` | 删除证书 |
| `POST` | `/api/resume/addSocialExperience` | 新增社会实践 |
| `POST` | `/api/resume/updateSocialExperience` | 更新社会实践 |
| `POST` | `/api/resume/deleteSocialExperience` | 删除社会实践 |

## 前端对接

用户端页面：

- `front-end/src/views/auth/PersonalProfile.vue`
- `front-end/src/views/auth/Resume.vue`

后台页面：

- `cot-admin-web/src/views/system/UserManage.vue`

## 数据隔离

用户资料和简历属于用户隐私数据。接口实现应优先从 JWT 获取当前用户 ID，或在查询参数中校验用户身份，避免用户读取或修改他人资料。

## 开发注意事项

- 修改简历子模块时，要同步更新完整简历聚合接口。
- 删除简历主体时，应考虑级联清理各子模块数据。
- 由于简历页面字段较多，接口字段命名要与前端表单保持一致。
