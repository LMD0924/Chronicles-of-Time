<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'academic-student-course',
  title: '学生课程管理',
  prefix: 'SC',
  description: '管理学生选课、课程成绩、重修次数、学期安排和通过状态。',
  categories: ['已通过', '未通过', '重修', '在读', '计划中'],
  auditPermission: 'academic:edit',
  deletePermission: 'academic:edit',
  remark: '对应 StudentCourse',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
