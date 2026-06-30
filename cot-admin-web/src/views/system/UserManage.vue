<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'system-user',
  title: '用户管理',
  prefix: 'U',
  description: '用户增删改查、启用禁用、重置密码、批量操作，适配 JWT + Redis 登录体系。',
  categories: ['学生', '教师', '运营', '升学顾问', '管理员'],
  auditPermission: 'sys:user:edit',
  deletePermission: 'sys:user:delete',
  remark: '支持重置密码、状态控制和批量操作',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
