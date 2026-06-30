<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/stores/app'

const visible = defineModel({ type: Boolean, default: false })
const appStore = useAppStore()
const colors = ['#2f9e8f', '#4f46e5', '#7c3aed', '#d97706', '#dc2626', '#0f766e', '#0ea5e9', '#16a34a']
const settings = computed(() => appStore.state)
const customColor = ref(settings.value.primaryColor)

const applyCustomColor = () => {
  if (!/^#[0-9a-fA-F]{6}$/.test(customColor.value)) {
    ElMessage.warning('请输入正确的 HEX 颜色值，例如 #2f9e8f')
    return
  }
  appStore.setPrimaryColor(customColor.value)
  ElMessage.success('自定义换肤已应用')
}
</script>

<template>
  <el-drawer v-model="visible" title="系统设置" size="340px">
    <div class="setting-section">
      <div class="setting-title">主题模式</div>
      <el-switch v-model="settings.dark" active-text="暗黑" inactive-text="亮色" @change="appStore.applyTheme" />
    </div>
    <el-divider />
    <div class="setting-section">
      <div class="setting-title">动态换肤</div>
      <div class="color-list">
        <button
          v-for="color in colors"
          :key="color"
          class="color-dot"
          :class="{ active: color === settings.primaryColor }"
          :style="{ backgroundColor: color }"
          type="button"
          @click="appStore.setPrimaryColor(color); customColor = color"
        ></button>
      </div>
      <div class="custom-color-row">
        <el-color-picker v-model="customColor" :predefine="colors" @change="applyCustomColor" />
        <el-input v-model="customColor" placeholder="#2f9e8f" maxlength="7" @keyup.enter="applyCustomColor" />
        <el-button type="primary" @click="applyCustomColor">应用</el-button>
      </div>
      <p class="setting-help">支持自定义品牌色，适合答辩演示、上线部署时快速切换视觉主题。</p>
    </div>
    <el-divider />
    <div class="setting-section switches">
      <el-checkbox v-model="settings.cachePages">页面缓存</el-checkbox>
      <el-checkbox v-model="settings.animation">路由动画</el-checkbox>
      <el-checkbox v-model="settings.showFooter">底部版权</el-checkbox>
    </div>
  </el-drawer>
</template>

<style scoped>
.setting-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.setting-title {
  font-size: 14px;
  font-weight: 800;
  color: var(--el-text-color-primary);
}

.color-list,
.custom-color-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.color-dot {
  width: 26px;
  height: 26px;
  border: 2px solid transparent;
  border-radius: 999px;
  cursor: pointer;
}

.color-dot.active {
  border-color: var(--el-text-color-primary);
}

.custom-color-row :deep(.el-input) {
  flex: 1;
}

.setting-help {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.6;
}

.switches {
  gap: 8px;
}
</style>

