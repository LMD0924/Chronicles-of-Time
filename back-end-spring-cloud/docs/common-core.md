# common-core

## 定位

`common-core` 是后端公共核心工具模块，不作为独立服务启动。业务服务通过 Maven 依赖它，复用通用配置和基础工具。

## 主要职责

- JWT 基础工具和 Token 相关能力。
- Jackson 序列化配置，解决前端 JavaScript 处理 Long 精度问题。
- Bean 拷贝、通用常量、基础工具类。
- 可放置跨服务无状态工具，避免各业务服务重复实现。

## 模块边界

适合放入 `common-core`：

- 与具体数据库无关的工具。
- 与具体业务表无关的基础能力。
- 多个服务都需要的通用配置。

不适合放入 `common-core`：

- 业务 DTO、VO、Entity。
- 服务间调用逻辑。
- 某一个业务服务独有的规则。
- 需要连接具体数据库的组件。

## 关键目录

```text
common-core
├─ pom.xml
└─ src/main
   ├─ java/org/example/commoncore
   │  ├─ config      # Jackson 等公共配置
   │  └─ utils       # 通用工具
   └─ resources
      └─ application.yml
```

## 使用方式

业务服务在 `pom.xml` 引入：

```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>common-core</artifactId>
</dependency>
```

父工程已统一管理版本，子模块不需要手动写版本号。

## 开发注意事项

- 该模块应保持轻量，避免引入 Web、数据库或外部服务强依赖。
- 公共工具要保持无状态，避免不同服务运行时互相影响。
- 公共配置会被业务服务扫描到时，必须确认不会与服务本地配置冲突。
