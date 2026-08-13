<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'
const moduleConfig = { key: 'chat-message', title: '聊天消息审查', description: '查看聊天消息并执行删除/撤回处理，不允许管理员伪造发送者或篡改会话归属。' }
</script>
<template><EnterpriseCrud :module="moduleConfig" /></template>