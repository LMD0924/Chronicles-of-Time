<!--
  文件说明：拾光记后台管理系统大学学业页面组件，承载大学学业场景的界面展示、交互操作和数据承接。
-->
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
