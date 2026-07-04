<!--
  文件说明：拾光记后台管理系统大学学业页面组件，承载大学学业场景的界面展示、交互操作和数据承接。
-->
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
