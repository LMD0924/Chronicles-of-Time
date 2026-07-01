<!--
  文件说明：拾光记后台管理系统系统管理页面组件，承载系统管理场景的界面展示、交互操作和数据承接。
-->
<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'system-perm',
  title: '权限管理',
  prefix: 'P',
  description: '按钮级权限控制、权限标识维护、接口权限映射。',
  categories: ['菜单权限', '按钮权限', '接口权限', '数据权限'],
  auditPermission: 'sys:role:assign',
  deletePermission: 'sys:user:delete',
  remark: '精准控制后台权限',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
