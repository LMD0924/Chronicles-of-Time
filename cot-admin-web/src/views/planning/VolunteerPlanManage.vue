<!--
  文件说明：拾光记后台管理系统高中升学规划页面组件，承载高中升学规划场景的界面展示、交互操作和数据承接。
-->
<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'plan-volunteer',
  title: '志愿方案管理',
  prefix: 'VOL',
  description: '管理高考志愿模拟、冲稳保方案、院校推荐、专业推荐和归档方案。',
  categories: ['冲刺', '稳妥', '保底', '待复核', '已归档'],
  auditPermission: 'academic:edit',
  deletePermission: 'content:delete',
  remark: '对应 VolunteerService',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
