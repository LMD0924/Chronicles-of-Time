<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'academic-gap',
  title: '毕业差距管理',
  prefix: 'GAP',
  description: '查看毕业要求差距、缺失课程、先修课缺口和毕业风险。',
  categories: ['缺必修', '缺选修', '缺学分', '先修缺口', '毕业风险'],
  auditPermission: 'academic:edit',
  deletePermission: 'academic:edit',
  remark: '对应 GraduationGap',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
