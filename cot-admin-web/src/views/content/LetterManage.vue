<!--
  文件说明：拾光记后台管理系统内容社区页面组件，承载内容社区场景的界面展示、交互操作和数据承接。
-->
<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'content-letter',
  title: '时光笺管理',
  prefix: 'LET',
  description: '查看用户慢递、寄语、遗憾内容，并进行审核管理。',
  categories: ['慢递', '寄语', '遗憾', '未来信件'],
  auditPermission: 'content:audit',
  deletePermission: 'content:delete',
  remark: '支持敏感内容审核',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
