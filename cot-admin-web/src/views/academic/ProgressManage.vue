<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'academic-progress',
  title: '学业进度管理',
  prefix: 'PRO',
  description: '查看所有学生学业完成进度、毕业进度统计和预警。',
  categories: ['学分进度', '毕业要求', 'GPA', '预警记录'],
  auditPermission: 'academic:edit',
  deletePermission: 'academic:edit',
  remark: '支持毕业进度统计',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
