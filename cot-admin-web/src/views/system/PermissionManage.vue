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
