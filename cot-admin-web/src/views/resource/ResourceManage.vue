<!--
  文件说明：拾光记后台管理系统文件资源页面组件，承载文件资源场景的界面展示、交互操作和数据承接。
-->
<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'resource-file',
  title: '文件资源管理',
  prefix: 'F',
  description: '全局图片 / 文件资源管理、上传记录、资源清理、违规文件删除。',
  categories: ['图片', '文档', '压缩包', '视频', '临时文件'],
  auditPermission: 'monitor:view',
  deletePermission: 'resource:delete',
  remark: '支持资源清理',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
