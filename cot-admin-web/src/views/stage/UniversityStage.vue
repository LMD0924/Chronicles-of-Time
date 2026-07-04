<!--
  文件说明：拾光记后台管理系统人生阶段页面组件，承载人生阶段场景的界面展示、交互操作和数据承接。
-->
<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'stage-university',
  title: '大学阶段管理',
  prefix: 'UNI',
  description: '面向大学探索期，管理专业路径、课程树、GPA 模拟、证书档案、论文进度、实习准备和毕业差距。',
  categories: ['专业路径', '课程树', 'GPA模拟', '证书档案', '论文进度', '实习准备'],
  auditPermission: 'academic:edit',
  deletePermission: 'academic:edit',
  remark: '帮助大学阶段少走弯路',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
