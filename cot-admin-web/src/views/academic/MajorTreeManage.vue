<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'academic-major',
  title: '专业树管理',
  prefix: 'MAJ',
  description: '维护专业、培养方案、课程节点，新增与编辑课程树。',
  categories: ['专业', '培养方案', '课程节点', '先修关系'],
  auditPermission: 'academic:edit',
  deletePermission: 'academic:edit',
  remark: '支持课程树维护',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
