<!--
  文件说明：拾光记后台管理系统内容社区页面组件，承载内容社区场景的界面展示、交互操作和数据承接。
-->
<script setup>
import EnterpriseCrud from '@/components/crud/EnterpriseCrud.vue'

const moduleConfig = {
  key: 'content-album',
  title: '相册图片管理',
  prefix: 'IMG',
  description: '批量查看图片、违规图片删除、图片审核与资源关联。',
  categories: ['校园相册', '毕业相册', '活动图片', '证书图片'],
  auditPermission: 'content:audit',
  deletePermission: 'content:delete',
  remark: '支持图片审核',
}
</script>

<template>
  <EnterpriseCrud :module="moduleConfig" />
</template>
