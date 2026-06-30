<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'content-comment',
  title: '评论管理',
  prefix: 'COM',
  description: '管理评论、回复、举报、敏感词屏蔽和违规处理。',
  categories: ['正常评论', '待审核', '举报评论', '已屏蔽'],
  auditPermission: 'content:audit',
  deletePermission: 'content:delete',
  remark: '对应 CommentService',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
