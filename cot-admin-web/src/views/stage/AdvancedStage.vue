<!--
  文件说明：拾光记后台管理系统人生阶段页面组件，承载人生阶段场景的界面展示、交互操作和数据承接。
-->
<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'stage-advanced',
  title: '进阶成长管理',
  prefix: 'ADV',
  description: '面向长期成长，管理年度目标、能力雷达、里程碑、知识体系、遗憾清单和未来信件。',
  categories: ['年度目标', '能力雷达', '里程碑', '知识体系', '遗憾清单', '未来信件'],
  auditPermission: 'content:audit',
  deletePermission: 'content:delete',
  remark: '把迷茫转化为可执行的成长路线',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
