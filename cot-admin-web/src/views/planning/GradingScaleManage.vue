<!--
  文件说明：拾光记后台管理系统高中升学规划页面组件，承载高中升学规划场景的界面展示、交互操作和数据承接。
-->
<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'plan-grading',
  title: '赋分规则管理',
  prefix: 'GRD',
  description: '维护等级赋分规则、区间、换算分、适用年级和生效状态。',
  categories: ['A等级', 'B等级', 'C等级', 'D等级', 'E等级'],
  auditPermission: 'academic:edit',
  deletePermission: 'content:delete',
  remark: '对应 GradingScale',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
