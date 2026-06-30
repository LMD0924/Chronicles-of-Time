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
