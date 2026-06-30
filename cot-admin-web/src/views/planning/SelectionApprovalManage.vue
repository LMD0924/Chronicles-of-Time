<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'plan-approval',
  title: '选科审批管理',
  prefix: 'APP',
  description: '统一处理学生选科审批、驳回、沟通意见和审批流记录。',
  categories: ['待审批', '已通过', '已驳回', '需沟通'],
  auditPermission: 'academic:edit',
  deletePermission: 'content:delete',
  remark: '对应 ApprovalPanel',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
