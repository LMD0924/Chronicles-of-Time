<!--
  文件说明：拾光记后台管理系统系统管理页面组件，承载系统管理场景的界面展示、交互操作和数据承接。
-->
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
