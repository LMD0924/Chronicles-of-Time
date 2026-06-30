<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <main class="forbidden-page">
    <el-result icon="warning" title="403 无后台权限" sub-title="普通账号没有进入拾光记后台管理系统的权限，请使用管理员账号登录。">
      <template #extra>
        <el-button type="primary" @click="router.push('/login')">返回登录</el-button>
      </template>
    </el-result>
  </main>
</template>

<style scoped>
.forbidden-page {
  display: grid;
  min-height: 100vh;
  place-items: center;
  background: var(--cot-bg);
}
</style>
