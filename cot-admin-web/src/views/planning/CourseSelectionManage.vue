<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'plan-selection',
  title: '选科管理',
  prefix: 'SEL',
  description: '管理学生 3+1+2 选科意向、组合、确认状态和历史变更。',
  categories: ['物化生', '物化地', '物生地', '史政地', '史政生'],
  auditPermission: 'academic:edit',
  deletePermission: 'content:delete',
  remark: '对应 StudentCourseSelection',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
