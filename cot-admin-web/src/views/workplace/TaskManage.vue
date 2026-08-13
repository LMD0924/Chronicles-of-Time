<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'
const moduleConfig = { key: 'work-task', title: '职场任务管理', description: '管理用户任务、截止日期、优先级、提醒和完成状态。' }
</script>
<template><EnterpriseCrud :module="moduleConfig" /></template>