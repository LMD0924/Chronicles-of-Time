<!--
  文件说明：拾光记后台管理系统大学学业页面组件，承载大学学业场景的界面展示、交互操作和数据承接。
-->
<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'academic-course-tree',
  title: '课程树管理',
  prefix: 'CT',
  description: '管理大学课程树、课程分类、先修关系、培养方案节点。',
  categories: ['公共基础', '专业核心', '专业选修', '实践课程', '先修关系'],
  auditPermission: 'academic:edit',
  deletePermission: 'academic:edit',
  remark: '对应 CourseTree',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
