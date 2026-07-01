<!--
  文件说明：拾光记后台管理系统人生阶段页面组件，承载人生阶段场景的界面展示、交互操作和数据承接。
-->
<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'stage-high',
  title: '高中阶段管理',
  prefix: 'HS',
  description: '面向曾经迷茫的高中自己，管理选科、目标大学、复习计划、错题沉淀、家长只读视图和关键节点提醒。',
  categories: ['选科决策', '目标大学', '复习计划', '错题复盘', '高考节点'],
  auditPermission: 'academic:edit',
  deletePermission: 'content:delete',
  remark: '弥补高中阶段信息差和目标感不足',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
