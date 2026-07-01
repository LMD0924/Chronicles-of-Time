<!--
  文件说明：拾光记后台管理系统后台框架布局页面组件，承载后台框架布局场景的界面展示、交互操作和数据承接。
-->
<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useFullscreen } from '@vueuse/core'
import { ElMessageBox } from 'element-plus'
import { Fold, FullScreen, Moon, Refresh, Setting, Sunny, SwitchButton, Expand } from '@element-plus/icons-vue'
import { adminMenus, iconMap } from '@/router/menus'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import SettingsDrawer from './SettingsDrawer.vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const { toggle: toggleFullscreen } = useFullscreen()
const settingsVisible = ref(false)

// 当前路由路径直接驱动侧边栏高亮，保证刷新和深链进入时菜单状态一致。
const activeMenu = computed(() => route.path)
const breadcrumbs = computed(() => route.matched.filter((item) => item.meta?.title))
const keepAliveNames = computed(() => appStore.cachedViews)



// 退出登录前二次确认，确认后清理 Pinia/localStorage 登录态并回到登录页。
const logout = async () => {
  await ElMessageBox.confirm('确认退出拾光记后台管理系统？', '退出登录', { type: 'warning' })
  userStore.logout()
  router.push('/login')
}

const reloadPage = () => {
  router.replace({ path: '/redirect' }).catch(() => {})
  window.location.reload()
}
</script>

<template>
  <el-container class="admin-layout">
    <!-- 侧边栏宽度由 Pinia 持久化状态控制，刷新后仍保持折叠或展开。 -->
<el-aside class="admin-sidebar" :width="appStore.collapsed ? '64px' : '248px'">
      <div class="brand" :class="{ collapsed: appStore.collapsed }">
        <div class="brand-mark">拾</div>
        <div v-show="!appStore.collapsed" class="brand-text">
          <strong>拾光记后台</strong>
          <span>For the once-lost me</span>
        </div>
      </div>

      <el-scrollbar class="sidebar-scrollbar">
        <el-menu :default-active="activeMenu" :collapse="appStore.collapsed" router unique-opened class="sidebar-menu">
          <template v-for="item in adminMenus" :key="item.path">
            <el-sub-menu v-if="item.children" :index="item.path">
              <template #title>
                <el-icon><component :is="iconMap[item.icon]" /></el-icon>
                <span>{{ item.title }}</span>
              </template>
              <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
                <el-icon><component :is="iconMap[child.icon]" /></el-icon>
                <template #title>{{ child.title }}</template>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="item.path">
              <el-icon><component :is="iconMap[item.icon]" /></el-icon>
              <template #title>{{ item.title }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-button text :icon="appStore.collapsed ? Expand : Fold" @click="appStore.collapsed = !appStore.collapsed" />
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item>拾光记</el-breadcrumb-item>
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">{{ item.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-actions">
          <el-tooltip content="刷新页面"><el-button text :icon="Refresh" @click="reloadPage" /></el-tooltip>
          <el-tooltip content="全屏"><el-button text :icon="FullScreen" @click="toggleFullscreen" /></el-tooltip>
          <el-tooltip :content="appStore.state.dark ? '亮色主题' : '暗黑主题'">
            <el-button text :icon="appStore.state.dark ? Sunny : Moon" @click="appStore.toggleDark" />
          </el-tooltip>
          <el-tooltip content="系统设置"><el-button text :icon="Setting" @click="settingsVisible = true" /></el-tooltip>
          <el-dropdown trigger="click">
            <div class="user-entry">
              <el-avatar :size="30">{{ userStore.userInfo?.nickname?.slice(0, 1) || '管' }}</el-avatar>
              <span>{{ userStore.userInfo?.nickname }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
                <el-dropdown-item>修改密码</el-dropdown-item>
                <el-dropdown-item divided :icon="SwitchButton" @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="admin-main">
                <!-- router-view + keep-alive 组合支持页面缓存，动画开关由系统设置统一控制。 -->
        <router-view v-slot="{ Component, route: currentRoute }">
          <transition :name="appStore.state.animation ? 'page' : ''" mode="out-in">
            <keep-alive :include="keepAliveNames">
              <component :is="Component" :key="currentRoute.fullPath" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>

      <el-footer v-if="appStore.state.showFooter" class="admin-footer">Copyright © 2026 拾光记 Chronicles of Time Admin. 弥补当时迷茫的自己。</el-footer>
    </el-container>

    <SettingsDrawer v-model="settingsVisible" />
  </el-container>
</template>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background: var(--cot-bg);
}

.admin-sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  overflow: hidden;
  border-right: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color);
  transition: width 0.2s ease;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 64px;
  padding: 0 16px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.brand.collapsed {
  justify-content: center;
  padding: 0;
}

.brand-mark {
  display: grid;
  width: 38px;
  height: 38px;
  flex: 0 0 38px;
  place-items: center;
  border-radius: 10px;
  color: white;
  font-size: 18px;
  font-weight: 900;
  background: linear-gradient(135deg, var(--cot-primary), #78c6b7);
}

.brand-text {
  min-width: 0;
  line-height: 1.2;
}

.brand-text strong,
.brand-text span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.brand-text span {
  margin-top: 3px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.sidebar-scrollbar {
  height: calc(100vh - 64px);
}

.sidebar-menu {
  border-right: 0;
}

.admin-header {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--el-border-color-light);
  background: color-mix(in srgb, var(--el-bg-color) 88%, transparent);
  backdrop-filter: blur(16px);
}

.header-left,
.header-actions,
.user-entry {
  display: flex;
  align-items: center;
  gap: 10px;
}

.breadcrumb {
  min-width: 0;
}

.user-entry {
  cursor: pointer;
  color: var(--el-text-color-primary);
  font-size: 14px;
  font-weight: 600;
}

.admin-main {
  min-height: calc(100vh - 112px);
  padding: 20px;
}

.admin-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 48px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

@media (max-width: 768px) {
  .admin-sidebar {
    display: none;
  }

  .breadcrumb,
  .user-entry span {
    display: none;
  }

  .admin-main {
    padding: 14px;
  }
}
</style>

