# file-upload

## 定位

`file-upload` 是文件上传服务，负责图片、文档、视频和其他文件的上传、分类存储、访问 URL 返回和文件删除。当前使用本地磁盘存储，后续可替换为对象存储。

## 端口和网关

```yaml
server:
  port: 8090
```

网关路径：

```text
/api/upload/**
```

服务直连示例：

```text
http://localhost:8090/api/upload/image
```

网关访问示例：

```text
http://localhost:8500/api/upload/image
```

## 存储配置

配置文件：

`file-upload/src/main/resources/application.yml`

关键配置：

```yaml
file:
  upload:
    base-path: E:/GitHub/Chronicles-Of-Time/back-end-spring-cloud/file-upload/uploads/
    image-path: images/
    video-path: videos/
    document-path: documents/
    other-path: others/
    access-url: http://localhost:8090/files/
    max-file-size: 10
    generate-thumbnail: true
```

本地上传目录会按类型拆分：

- `images/`
- `videos/`
- `documents/`
- `others/`

## 上传限制

Spring Multipart：

- 单文件最大：`10MB`
- 单次请求最大：`20MB`

文件服务内部限制：

- 单文件最大：`10MB`
- 支持图片 MIME：`jpeg`、`jpg`、`png`、`gif`、`webp`、`bmp`
- 支持视频 MIME：`mp4`、`mpeg`、`quicktime`
- 支持文档 MIME：`pdf`、`doc`、`docx`、`xls`、`xlsx`、`txt`

## 主要接口

基础路径：

```text
/api/upload
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/upload/file` | 上传单个通用文件 |
| `POST` | `/api/upload/image` | 上传单张图片 |
| `POST` | `/api/upload/files` | 批量上传文件 |
| `DELETE` | `/api/upload/file/{fileId}` | 删除文件 |

## 关键目录

```text
file-upload
├─ src/main/java/org/example/fileupload
│  ├─ config
│  ├─ controller/UploadController.java
│  ├─ entity
│  └─ service
├─ src/main/resources/application.yml
└─ uploads
```

## 运维注意事项

- `base-path` 是本地绝对路径，换机器后要修改。
- 如果通过网关访问上传接口，但返回的 `access-url` 是 `8090`，浏览器需要能直接访问文件服务端口。
- 生产环境建议把文件存储迁移到对象存储，并把 `access-url` 改成 CDN 或对象存储访问域名。
- 上传接口需要鉴权时，要确认网关未把 `/api/upload/**` 加入白名单。
