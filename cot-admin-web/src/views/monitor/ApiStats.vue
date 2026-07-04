<!--
  文件说明：拾光记后台管理系统系统监控页面组件，承载系统监控场景的界面展示、交互操作和数据承接。
-->
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
