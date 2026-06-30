<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'system-menu',
  title: '菜单管理',
  prefix: 'M',
  description: '动态侧边栏菜单配置、多级菜单维护、图标与路由绑定。',
  categories: ['目录', '菜单', '按钮', '外链'],
  auditPermission: 'sys:menu:edit',
  deletePermission: 'sys:user:delete',
  remark: '支持多级菜单配置',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
