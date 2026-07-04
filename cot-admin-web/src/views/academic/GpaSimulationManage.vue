<!--
  文件说明：拾光记后台管理系统大学学业页面组件，承载大学学业场景的界面展示、交互操作和数据承接。
-->
<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'academic-gpa',
  title: 'GPA 模拟管理',
  prefix: 'GPA',
  description: '管理 GPA 模拟记录、课程成绩假设、绩点预测和提升方案。',
  categories: ['当前GPA', '目标GPA', '模拟方案', '提升建议'],
  auditPermission: 'academic:edit',
  deletePermission: 'academic:edit',
  remark: '对应 GpaSimulator',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
