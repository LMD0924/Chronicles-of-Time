<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useTheme } from '@/composables/useTheme'
import {
  getStoredTheme,
  getStoredThemeColor,
  setTheme,
  setThemeColor,
  THEME_COLOR_PRESETS,
  ThemeType
} from '@/utils/theme'
import Nav from '@/components/Nav.vue'
import request from '@/utils/request'

const router = useRouter()
const { isDark, themeColor } = useTheme()
const [messageApi, contextHolder] = message.useMessage()

const user = ref({})
const currentTheme = ref(getStoredTheme())
const selectedPreset = ref(getStoredThemeColor().preset || getStoredThemeColor().key)
const customColor = ref({
  primary: getStoredThemeColor().primary,
  secondary: getStoredThemeColor().secondary
})
const prefs = ref({
  notifyGrowth: true,
  notifyExam: true,
  privacyHideScore: false,
  rememberLogin: !!localStorage.getItem('token'),
})

const STORAGE_PREFS = 'app_user_prefs'

const previewStyle = computed(() => ({
  background: `linear-gradient(135deg, ${customColor.value.primary}, ${customColor.value.secondary})`
}))

const load = async () => {
  const saved = localStorage.getItem(STORAGE_PREFS)
  if (saved) Object.assign(prefs.value, JSON.parse(saved))
  try {
    const res = await request.get('/user/getUserById')
    if (res.code === 200) user.value = res.data || {}
  } catch (_) {}
}

const savePrefs = () => {
  localStorage.setItem(STORAGE_PREFS, JSON.stringify(prefs.value))
  messageApi.success('偏好已保存')
}

const setMode = (mode) => {
  currentTheme.value = mode
  setTheme(mode)
}

const pickPreset = (preset) => {
  selectedPreset.value = preset.key
  customColor.value = {
    primary: preset.primary,
    secondary: preset.secondary
  }
  setThemeColor({ preset: preset.key, primary: preset.primary, secondary: preset.secondary })
  messageApi.success(`已切换为${preset.name}主题`)
}

const saveCustomTheme = () => {
  selectedPreset.value = 'custom'
  setThemeColor({
    preset: 'custom',
    name: '自定义',
    primary: customColor.value.primary,
    secondary: customColor.value.secondary
  })
  messageApi.success('自定义主题色已应用')
}

const clearCache = () => {
  const keep = ['token', 'refresh_token', 'app_theme', 'app_theme_color', STORAGE_PREFS]
  Object.keys(localStorage).forEach((k) => {
    if (!keep.includes(k)) localStorage.removeItem(k)
  })
  messageApi.success('已清理本地缓存（保留登录与主题）')
}

onMounted(load)
</script>

<template>
  <contextHolder />
  <div class="app-shell" :class="isDark ? 'app-shell-dark' : 'app-shell-light'">
    <Nav :isDark="isDark" :showBackHome="true" />
    <main class="app-main">
      <div class="app-container max-w-4xl space-y-6">
        <div>
          <p class="app-section-label mb-2">Account</p>
          <h1 class="app-page-title">设置</h1>
          <p class="app-page-desc">统一管理外观、主题色、通知偏好和账号入口。</p>
        </div>

        <div class="app-card-surface p-6 space-y-6">
          <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
            <div>
              <h3 class="text-lg font-black text-slate-900 dark:text-white">外观主题</h3>
              <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">主题色会同步到全站按钮、卡片、导航、表单和重点信息。</p>
            </div>
            <div class="flex rounded-2xl border border-brand-200/70 bg-white/70 p-1 dark:border-white/10 dark:bg-white/5">
              <button
                type="button"
                class="settings-mode-btn"
                :class="currentTheme === ThemeType.LIGHT ? 'settings-mode-active' : 'settings-mode-idle'"
                @click="setMode(ThemeType.LIGHT)"
              >
                浅色
              </button>
              <button
                type="button"
                class="settings-mode-btn"
                :class="currentTheme === ThemeType.DARK ? 'settings-mode-active' : 'settings-mode-idle'"
                @click="setMode(ThemeType.DARK)"
              >
                深色
              </button>
            </div>
          </div>

          <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
            <button
              v-for="preset in THEME_COLOR_PRESETS"
              :key="preset.key"
              type="button"
              class="settings-preset"
              :class="selectedPreset === preset.key ? 'settings-preset-active' : ''"
              @click="pickPreset(preset)"
            >
              <span class="settings-swatch" :style="{ background: `linear-gradient(135deg, ${preset.primary}, ${preset.secondary})` }" />
              <span class="font-bold">{{ preset.name }}</span>
            </button>
          </div>

          <div class="grid gap-5 rounded-2xl border border-brand-100 bg-brand-50/50 p-4 dark:border-white/10 dark:bg-white/5 md:grid-cols-[1fr_auto] md:items-center">
            <div class="space-y-4">
              <div class="grid gap-4 sm:grid-cols-2">
                <label class="settings-color-field">
                  <span>主色</span>
                  <input v-model="customColor.primary" type="color" />
                  <code>{{ customColor.primary }}</code>
                </label>
                <label class="settings-color-field">
                  <span>辅色</span>
                  <input v-model="customColor.secondary" type="color" />
                  <code>{{ customColor.secondary }}</code>
                </label>
              </div>
              <button type="button" class="app-btn-primary text-sm" @click="saveCustomTheme">应用自定义主题色</button>
            </div>

            <div class="settings-theme-preview" :style="previewStyle">
              <span>Preview</span>
              <strong>拾光记</strong>
            </div>
          </div>
        </div>

        <div class="grid gap-6 lg:grid-cols-2">
          <div class="app-card-surface p-6 space-y-3">
            <h3 class="font-black text-slate-900 dark:text-white">通知与隐私</h3>
            <label class="settings-switch-row">
              <span>成长记录提醒</span>
              <input v-model="prefs.notifyGrowth" type="checkbox" class="rounded border-brand-300 text-brand-600" />
            </label>
            <label class="settings-switch-row">
              <span>考试/成绩提醒</span>
              <input v-model="prefs.notifyExam" type="checkbox" class="rounded border-brand-300 text-brand-600" />
            </label>
            <label class="settings-switch-row">
              <span>家长视图隐藏成绩</span>
              <input v-model="prefs.privacyHideScore" type="checkbox" class="rounded border-brand-300 text-brand-600" />
            </label>
            <button type="button" class="app-btn-primary text-sm mt-2" @click="savePrefs">保存偏好</button>
          </div>

          <div class="app-card-surface p-6 space-y-3">
            <h3 class="font-black text-slate-900 dark:text-white">账号</h3>
            <p class="text-sm text-zinc-500">{{ user.name || '未命名用户' }} · {{ user.email || user.phone || '未绑定' }}</p>
            <div class="flex flex-wrap gap-3">
              <button type="button" class="app-btn-secondary text-sm" @click="router.push('/PersonalProfile')">编辑个人档案</button>
              <button type="button" class="app-btn-secondary text-sm" @click="router.push('/Resume')">编辑简历</button>
              <button type="button" class="app-btn-secondary text-sm" @click="router.push('/PrePare')">大学信息绑定</button>
            </div>
          </div>
        </div>

        <div class="app-card-surface p-6">
          <h3 class="font-black text-slate-900 dark:text-white mb-2">数据</h3>
          <button type="button" class="text-sm font-semibold text-red-600 hover:underline" @click="clearCache">清理本地缓存</button>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.settings-mode-btn {
  min-width: 4.8rem;
  border-radius: 12px;
  padding: 0.5rem 0.9rem;
  font-size: 0.875rem;
  font-weight: 800;
  transition: all 180ms ease;
}

.settings-mode-active {
  color: white;
  background: var(--theme-gradient);
  box-shadow: 0 12px 24px -18px rgba(var(--theme-primary-rgb), 0.85);
}

.settings-mode-idle {
  color: var(--app-text-secondary);
}

.settings-mode-idle:hover {
  color: rgb(var(--color-brand-600));
  background: rgba(var(--theme-primary-rgb), 0.08);
}

.settings-preset {
  display: flex;
  min-height: 4.25rem;
  align-items: center;
  gap: 0.75rem;
  border: 1px solid var(--app-card-border);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.66);
  padding: 0.8rem;
  color: var(--app-text);
  text-align: left;
  transition: all 180ms ease;
}

html.dark .settings-preset {
  background: rgba(255, 255, 255, 0.05);
}

.settings-preset:hover,
.settings-preset-active {
  border-color: rgba(var(--theme-primary-rgb), 0.42);
  box-shadow: 0 16px 34px -28px rgba(var(--theme-primary-rgb), 0.72);
  transform: translateY(-1px);
}

.settings-swatch {
  width: 2.35rem;
  height: 2.35rem;
  flex: 0 0 auto;
  border-radius: 12px;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.45);
}

.settings-color-field {
  display: grid;
  grid-template-columns: auto auto 1fr;
  align-items: center;
  gap: 0.75rem;
  color: var(--app-text-secondary);
  font-size: 0.9rem;
  font-weight: 700;
}

.settings-color-field input {
  width: 2.75rem;
  height: 2.2rem;
  border: 0;
  border-radius: 10px;
  background: transparent;
}

.settings-color-field code {
  color: var(--app-text-muted);
  font-size: 0.78rem;
}

.settings-theme-preview {
  display: flex;
  min-height: 7.5rem;
  min-width: 12rem;
  flex-direction: column;
  justify-content: flex-end;
  border-radius: 18px;
  padding: 1rem;
  color: white;
  box-shadow: 0 20px 42px -26px rgba(var(--theme-primary-rgb), 0.85);
}

.settings-theme-preview span {
  font-size: 0.72rem;
  font-weight: 800;
  opacity: 0.75;
  text-transform: uppercase;
}

.settings-theme-preview strong {
  font-size: 1.35rem;
  line-height: 1;
}

.settings-switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  border-radius: 14px;
  padding: 0.72rem 0;
  color: var(--app-text-secondary);
  font-size: 0.92rem;
}
</style>
