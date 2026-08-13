<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'
const moduleConfig = { key: 'chat-friend', title: '好友关系管理', description: '管理用户好友关系、备注和关系状态。' }
</script>
<template><EnterpriseCrud :module="moduleConfig" /></template>