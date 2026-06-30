<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'learn-graph',
  title: '知识图谱管理',
  prefix: 'KG',
  description: '管理用户知识图谱节点、边关系、掌握度、薄弱路径和学习路径推荐。',
  categories: ['节点', '关系', '掌握度', '学习路径', '薄弱链路'],
  auditPermission: 'academic:edit',
  deletePermission: 'content:delete',
  remark: '对应 KnowledgeGraph / KnowledgeNode / KnowledgeEdge',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
