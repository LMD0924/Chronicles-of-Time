<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'academic-paper',
  title: '论文管理',
  prefix: 'PAP',
  description: '查看所有用户毕设论文、版本记录、进度审核。',
  categories: ['开题', '中期', '定稿', '答辩'],
  auditPermission: 'academic:edit',
  deletePermission: 'academic:edit',
  remark: '支持论文版本审核',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
