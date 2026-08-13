<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'
const moduleConfig = { key: 'chat-group', title: '聊天群组管理', description: '管理群号、群名称、公告、搜索权限、全员禁言和置顶消息。' }
</script>
<template><EnterpriseCrud :module="moduleConfig" /></template>