<!--
  文件说明：拾光记后台管理系统系统管理页面组件，承载系统管理场景的界面展示、交互操作和数据承接。
-->
<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'system-log',
  title: '日志管理',
  prefix: 'L',
  description: '登录日志、操作日志、异常日志检索、删除与导出。',
  categories: ['登录日志', '操作日志', '异常日志', '安全日志'],
  auditPermission: 'monitor:view',
  deletePermission: 'sys:log:delete',
  remark: '完整审计链路',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
