# university-service

## 定位

`university-service` 是大学阶段学业服务，覆盖大学专业、课程树、课程分类、学生课程成绩、毕业进度、GPA 模拟、课程安排和论文建议。

## 端口和数据库

```yaml
server:
  port: 8083
```

数据库：

```yaml
cot_university
```

注意：当前配置中的 `spring.application.name` 写成了 `high-service`，实际模块是 `university-service`。如果后续接入注册中心，应改为 `university-service`。

## 关键目录

```text
university-service
├─ src/main/java/org/example/universityservice
│  ├─ controller
│  │  ├─ major      # 专业、课程、课程分类、学生课程
│  │  └─ paper      # 论文和建议
│  ├─ entity
│  ├─ mapper
│  └─ service
└─ src/main/resources
   ├─ application.yml
   └─ mapper
```

## 主要对象

- `Major`：大学专业。
- `Course`：课程。
- `CourseCategory`：课程分类。
- `GraduationRequirement`：毕业要求。
- `StudentCourse`：学生课程和成绩。
- `Paper`：论文。
- `Suggestion`：论文建议。

## 主要接口

专业：

```text
/api/university/major
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/list` | 专业列表 |
| `GET` | `/{id}` | 专业详情 |
| `POST` | `/create` | 新增专业 |
| `PUT` | `/update` | 更新专业 |
| `DELETE` | `/{id}` | 删除专业 |
| `GET` | `/compare` | 专业对比 |

课程：

```text
/api/course
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/list` | 课程列表 |
| `GET` | `/tree` | 课程树 |
| `GET` | `/by-term` | 按学期查询课程 |
| `POST` | `/create` | 新增课程 |
| `PUT` | `/update` | 更新课程 |
| `DELETE` | `/{id}` | 删除课程 |
| `GET` | `/{id}` | 课程详情 |
| `GET` | `/search` | 搜索课程 |

课程分类：

```text
/api/course-category
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/list` | 分类列表 |
| `GET` | `/root` | 根分类 |
| `GET` | `/{id}` | 分类详情 |
| `POST` | `/create` | 新增分类 |
| `PUT` | `/update` | 更新分类 |
| `DELETE` | `/{id}` | 删除分类 |

学生课程：

```text
/api/student-course
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/list` | 学生课程列表 |
| `GET` | `/progress` | 学业进度 |
| `POST` | `/score` | 录入或更新成绩 |
| `GET` | `/gap-analysis` | 毕业差距分析 |
| `GET` | `/schedule` | 学期课程安排 |
| `POST` | `/simulate-gpa` | GPA 模拟 |

论文：

```text
/api/paper
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/list` | 论文列表 |
| `GET` | `/{id}` | 论文详情 |
| `POST` | `/create` | 新增论文 |
| `PUT` | `/update` | 更新论文 |
| `DELETE` | `/{id}` | 删除论文 |
| `GET` | `/suggestions/{paperId}` | 论文建议列表 |
| `POST` | `/suggestion/add` | 新增论文建议 |
| `DELETE` | `/suggestion/{id}` | 删除论文建议 |

## 前端对接

用户端：

- `front-end/src/views/university/Prepare.vue`
- `front-end/src/views/university/paper/Paper.vue`
- `front-end/src/views/university/paper/CourseTree.vue`
- `front-end/src/views/university/features/*`

后台端：

- `cot-admin-web/src/views/academic/MajorTreeManage.vue`
- `cot-admin-web/src/views/academic/CourseTreeManage.vue`
- `cot-admin-web/src/views/academic/StudentCourseManage.vue`
- `cot-admin-web/src/views/academic/ProgressManage.vue`
- `cot-admin-web/src/views/academic/GraduationGapManage.vue`
- `cot-admin-web/src/views/academic/GpaSimulationManage.vue`
- `cot-admin-web/src/views/academic/CertificateManage.vue`
- `cot-admin-web/src/views/academic/PaperManage.vue`

## 业务注意事项

- 课程树和毕业要求属于专业维度数据，修改后会影响毕业差距和学业进度计算。
- GPA 模拟应明确采用的绩点换算规则，避免与学校真实规则混淆。
- 论文建议可以作为导师反馈或系统建议，需保留来源字段时同步扩展实体和页面。
