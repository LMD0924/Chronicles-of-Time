<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'learn-question',
  title: '题目管理',
  prefix: 'Q',
  description: '后台统一管理题库 QuestionBank：题干、答案、解析、学段、学科、题型、难度、知识点、使用次数和错题率。',
  categories: ['数学', '语文', '英语', '物理', '化学', '生物', '历史', '政治', '地理'],
  auditPermission: 'academic:edit',
  deletePermission: 'content:delete',
  remark: '对应 general-service QuestionBankController',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
