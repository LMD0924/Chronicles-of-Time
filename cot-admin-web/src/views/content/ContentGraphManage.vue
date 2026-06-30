<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'content-graph',
  title: '内容知识图谱管理',
  prefix: 'CG',
  description: '管理文章知识图谱、内容节点、内容边关系和文章结构分析。',
  categories: ['内容节点', '内容关系', '文章结构', '主题聚类'],
  auditPermission: 'content:audit',
  deletePermission: 'content:delete',
  remark: '对应 ContentKnowledgeGraph',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
