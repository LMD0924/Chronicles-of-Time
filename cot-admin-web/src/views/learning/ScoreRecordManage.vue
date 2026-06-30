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
