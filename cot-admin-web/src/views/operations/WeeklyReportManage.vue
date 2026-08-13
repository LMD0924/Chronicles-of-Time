<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'
const moduleConfig = { key: 'ops-weekly-report', title: '成长周报管理', description: '查看和维护用户成长周报、反思内容及下周计划。' }
</script>
<template><EnterpriseCrud :module="moduleConfig" /></template>