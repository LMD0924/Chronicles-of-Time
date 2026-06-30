<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'monitor-online',
  title: '在线用户监控',
  prefix: 'ON',
  description: '查看 Redis 在线会话、踢出用户、登录设备与 IP 追踪。',
  categories: ['后台管理员', '学生用户', '教师用户', '顾问用户'],
  auditPermission: 'monitor:view',
  deletePermission: 'sys:log:delete',
  remark: '支持在线会话管理',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
