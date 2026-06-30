<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'content-moment',
  title: '校园动态管理',
  prefix: 'D',
  description: '审核用户动态、违规内容屏蔽、置顶、下架。',
  categories: ['校园动态', '活动记录', '社团分享', '公告互动'],
  auditPermission: 'content:audit',
  deletePermission: 'content:delete',
  remark: '支持置顶和违规屏蔽',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
