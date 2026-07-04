<!--
  文件说明：拾光记后台管理系统学习中心页面组件，承载学习中心场景的界面展示、交互操作和数据承接。
-->
<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'learn-answer',
  title: '答题记录管理',
  prefix: 'ANS',
  description: '查看答题流水、用户答案、正确性、耗时、题型、知识点与练习来源。',
  categories: ['正确', '错误', '跳过', '批量练习', '专项练习'],
  auditPermission: 'monitor:view',
  deletePermission: 'content:delete',
  remark: '对应 AnswerRecords',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
