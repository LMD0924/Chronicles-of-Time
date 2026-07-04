<!--
  文件说明：拾光记后台管理系统拾光记项目页面组件，承载拾光记项目场景的界面展示、交互操作和数据承接。
-->
<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123', remember: true })

const rules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }],
}

const submit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push(route.query.redirect ? decodeURIComponent(route.query.redirect) : '/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-visual">
      <img src="https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=1800&q=80" alt="拾光记后台管理" />
      <div class="visual-mask"></div>
      <div class="visual-content">
        <div class="brand-row"><span>拾</span><strong>拾光记后台管理系统</strong></div>
        <h1>弥补当时迷茫的自己，把高中、大学、职场与进阶成长都整理成清晰路径</h1>
        <p>适配 JWT + Redis 登录体系，统一走 /api 网关前缀，覆盖高中选择、大学学业、职场记录、进阶成长和内容治理。</p>
      </div>
    </section>

    <section class="login-form-wrap">
      <div class="login-card">
        <div class="login-head">
          <span>Admin Portal</span>
          <h2>管理员登录</h2>
          <p>演示账号：admin / admin123；普通账号 guest 会被权限系统拦截。</p>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
          <el-form-item prop="username"><el-input v-model="form.username" :prefix-icon="User" placeholder="管理员账号" /></el-form-item>
          <el-form-item prop="password"><el-input v-model="form.password" :prefix-icon="Lock" type="password" show-password placeholder="登录密码" /></el-form-item>
          <div class="login-options"><el-checkbox v-model="form.remember">记住登录状态</el-checkbox><el-link type="primary">忘记密码</el-link></div>
          <el-button type="primary" size="large" class="login-button" :loading="loading" @click="submit">登录后台</el-button>
        </el-form>
      </div>
    </section>
  </main>
</template>

<style scoped>
.login-page {
  display: grid;
  min-height: 100vh;
  grid-template-columns: minmax(0, 1.08fr) minmax(460px, 0.92fr);
  background: #f6f8fb;
}
.login-visual {
  position: relative;
  overflow: hidden;
}
.login-visual img,
.visual-mask {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}
.login-visual img {
  object-fit: cover;
}
.visual-mask {
  background: linear-gradient(135deg, rgba(16, 24, 39, 0.92), rgba(47, 158, 143, 0.62));
}
.visual-content {
  position: relative;
  z-index: 1;
  display: flex;
  height: 100%;
  flex-direction: column;
  justify-content: space-between;
  padding: 56px;
  color: white;
}
.brand-row {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
}
.brand-row span {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.16);
  font-weight: 900;
}
.visual-content h1 {
  max-width: 760px;
  margin: auto 0 0;
  font-size: 48px;
  line-height: 1.12;
}
.visual-content p {
  max-width: 680px;
  margin: 20px 0 0;
  color: rgba(255, 255, 255, 0.78);
  line-height: 1.8;
}
.login-form-wrap {
  display: grid;
  place-items: center;
  padding: 32px;
}
.login-card {
  width: min(420px, 100%);
  padding: 34px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 16px;
  background: white;
  box-shadow: 0 28px 80px -46px rgba(15, 23, 42, 0.55);
}
.login-head span {
  color: var(--cot-primary);
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
}
.login-head h2 {
  margin: 10px 0 8px;
  color: #172033;
  font-size: 30px;
}
.login-head p {
  margin: 0 0 28px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}
.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}
.login-button {
  width: 100%;
}
@media (max-width: 960px) {
  .login-page { grid-template-columns: 1fr; }
  .login-visual { display: none; }
}
</style>

