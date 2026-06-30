<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'stage-work',
  title: '职场阶段管理',
  prefix: 'WK',
  description: '面向初入职场的自己，管理简历版本、面试记录、项目复盘、技能成长、Offer 对比和职业方向。',
  categories: ['简历版本', '面试记录', '项目复盘', '技能成长', 'Offer对比', '职业方向'],
  auditPermission: 'academic:edit',
  deletePermission: 'content:delete',
  remark: '沉淀职场选择和成长轨迹',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
