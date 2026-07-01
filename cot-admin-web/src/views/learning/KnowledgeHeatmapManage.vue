<!--
  文件说明：拾光记后台管理系统学习中心页面组件，承载学习中心场景的界面展示、交互操作和数据承接。
-->
<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'learn-heat',
  title: '知识热力图管理',
  prefix: 'HEAT',
  description: '查看知识点热力、趋势、雷达图和不同学科掌握度分布。',
  categories: ['热力图', '趋势图', '雷达图', '学科对比'],
  auditPermission: 'monitor:view',
  deletePermission: 'content:delete',
  remark: '对应 knowledge heatmap/trend/radar',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
