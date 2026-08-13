<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'
const moduleConfig = { key: 'chat-group-member', title: '群成员管理', description: '管理群成员角色、状态、禁言截止时间和已读时间。' }
</script>
<template><EnterpriseCrud :module="moduleConfig" /></template>