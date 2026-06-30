<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'learn-mistake',
  title: '错题管理',
  prefix: 'MIST',
  description: '查看所有用户错题记录、掌握状态、错误答案、正确答案、复习次数和高频薄弱知识点。',
  categories: ['未掌握', '已复习', '已掌握', '高频错题'],
  auditPermission: 'academic:edit',
  deletePermission: 'content:delete',
  remark: '对应 MistakeRecordController',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
