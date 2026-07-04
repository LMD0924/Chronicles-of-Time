<!--
  文件说明：拾光记后台管理系统学习中心页面组件，承载学习中心场景的界面展示、交互操作和数据承接。
-->
<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'learn-score',
  title: '成绩记录管理',
  prefix: 'SCR',
  description: '管理考试成绩、弱科分析、成绩趋势、最佳科目和薄弱科目。',
  categories: ['月考', '期中', '期末', '模考', '竞赛'],
  auditPermission: 'academic:edit',
  deletePermission: 'content:delete',
  remark: '对应 ScoreRecordController',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
