<!--
  文件说明：拾光记后台管理系统系统监控页面组件，承载系统监控场景的界面展示、交互操作和数据承接。
-->
<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'monitor-service',
  title: '服务状态监控',
  prefix: 'SVC',
  description: '监控 gateway、auth-center、user-center、high-service、university-service、general-service、file-upload。',
  categories: ['健康', '告警', '降级', '离线'],
  auditPermission: 'monitor:view',
  deletePermission: 'sys:log:delete',
  remark: '微服务健康巡检',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
