<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'monitor-api',
  title: '接口访问统计',
  prefix: 'API',
  description: '统计接口访问量、平均耗时、错误率和慢接口趋势。',
  categories: ['认证接口', '用户接口', '内容接口', '选科接口', '文件接口'],
  auditPermission: 'monitor:view',
  deletePermission: 'sys:log:delete',
  remark: '支持接口性能分析',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
