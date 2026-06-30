<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'plan-guidance',
  title: '选科指导管理',
  prefix: 'GUI',
  description: '管理教师/顾问对学生选科的指导建议、风险提示和目标专业匹配。',
  categories: ['专业匹配', '风险提示', '家长建议', '教师建议'],
  auditPermission: 'academic:edit',
  deletePermission: 'content:delete',
  remark: '对应 CourseGuidance',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
