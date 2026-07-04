# common-db

## 定位

`common-db` 是后端数据库和响应公共模块，不作为独立服务启动。它沉淀统一响应结构、结果码、MyBatis-Plus 公共配置等与数据访问相关的基础能力。

## 主要能力

- `RestBean<T>`：统一接口响应结构。
- `ResultCodeEnum`：统一结果码枚举。
- MyBatis-Plus 分页插件配置。
- 可扩展公共数据访问配置，例如分页、逻辑删除、填充策略等。

## 统一响应

后端接口默认返回：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

常用构造方式：

```java
return RestBean.success(data);
return RestBean.success("保存成功", data);
return RestBean.fail("保存失败");
return RestBean.fail(401, "未登录");
```

前台和后台的 `request` 工具都会按 `code === 200` 判断成功。

## 模块边界

适合放入 `common-db`：

- 数据库相关公共配置。
- 统一响应、结果码。
- 通用分页和查询辅助对象。

不适合放入 `common-db`：

- 某个业务服务的 Mapper。
- 业务实体。
- 具体服务的 SQL。
- 跨数据库业务事务逻辑。

## 关键目录

```text
common-db
├─ pom.xml
└─ src/main
   ├─ java/org/example/commondb
   │  ├─ config
   │  └─ utils
   └─ resources
      └─ application.yml
```

## 使用方式

业务服务在 `pom.xml` 引入：

```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>common-db</artifactId>
</dependency>
```

## 开发注意事项

- 变更 `RestBean` 字段会影响前台、后台和所有后端接口，需谨慎。
- 新增全局 MyBatis-Plus 配置时，要检查所有服务是否兼容。
- 不要在公共模块里写业务库连接配置。
