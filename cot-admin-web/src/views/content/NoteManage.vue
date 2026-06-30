<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'content-note',
  title: '时光笔记管理',
  prefix: 'N',
  description: '查看所有用户笔记、审核、下架、删除、关键词检索。',
  categories: ['学习复盘', '成长记录', '校园生活', '职业记录'],
  auditPermission: 'content:audit',
  deletePermission: 'content:delete',
  remark: '支持内容审核治理',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
