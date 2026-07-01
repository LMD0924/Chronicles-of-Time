<!--
  文件说明：拾光记后台管理系统系统管理页面组件，承载系统管理场景的界面展示、交互操作和数据承接。
-->
<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'system-role',
  title: '角色管理',
  prefix: 'R',
  description: '角色新增、角色禁用、权限分配，支撑 RBAC 权限体系。',
  categories: ['超级管理员', '运营管理员', '教研管理员', '升学顾问', '只读审计'],
  auditPermission: 'sys:role:assign',
  deletePermission: 'sys:user:delete',
  remark: '支持角色权限分配',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
