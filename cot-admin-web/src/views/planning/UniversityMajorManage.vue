<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'plan-major',
  title: '院校专业库管理',
  prefix: 'UM',
  description: '管理高校、专业、招生计划、录取模拟和专业选科要求。',
  categories: ['高校库', '专业库', '招生计划', '录取模拟', '选科要求'],
  auditPermission: 'academic:edit',
  deletePermission: 'content:delete',
  remark: '对应 University/Major/AdmissionPlan',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
