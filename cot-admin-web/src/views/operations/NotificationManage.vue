<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'
const moduleConfig = { key: 'ops-notification', title: '通知中心管理', description: '管理逾期任务、复习提醒、面试提醒和系统通知的已读与忽略状态。' }
</script>
<template><EnterpriseCrud :module="moduleConfig" /></template>