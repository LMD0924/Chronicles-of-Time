<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'academic-cert',
  title: '证书档案管理',
  prefix: 'CERT',
  description: '管理学生证书、竞赛奖项、资格认证和附件材料。',
  categories: ['语言证书', '竞赛奖项', '职业证书', '荣誉证明'],
  auditPermission: 'academic:edit',
  deletePermission: 'academic:edit',
  remark: '对应 CertificateArchive',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
