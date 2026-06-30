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
