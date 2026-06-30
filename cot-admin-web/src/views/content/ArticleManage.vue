<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'content-article',
  title: '文章内容管理',
  prefix: 'ART',
  description: '管理社区文章、发布审核、点赞收藏、分类标签和下架删除。',
  categories: ['学习规划', '志愿填报', '大学成长', '职场记录', '社区公告'],
  auditPermission: 'content:audit',
  deletePermission: 'content:delete',
  remark: '对应 ContentController',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
