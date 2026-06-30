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
