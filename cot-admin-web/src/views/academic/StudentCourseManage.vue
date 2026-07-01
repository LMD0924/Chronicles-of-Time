<!--
  文件说明：拾光记后台管理系统大学学业页面组件，承载大学学业场景的界面展示、交互操作和数据承接。
-->
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
