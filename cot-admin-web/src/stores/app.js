import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'
import { SETTINGS_KEY } from '@/utils/auth'

const defaults = {
  collapsed: false,
  dark: false,
  primaryColor: '#2f9e8f',
  showFooter: true,
  animation: true,
  cachePages: true,
}

const normalizeHex = (color) => {
  if (!color) return defaults.primaryColor
  if (/^#[0-9a-fA-F]{6}$/.test(color)) return color.toLowerCase()
  if (/^#[0-9a-fA-F]{3}$/.test(color)) {
    const chars = color.slice(1).split('')
    return `#${chars.map((item) => item + item).join('')}`.toLowerCase()
  }
  return defaults.primaryColor
}

const hexToRgb = (hex) => {
  const value = normalizeHex(hex).slice(1)
  return {
    r: parseInt(value.slice(0, 2), 16),
    g: parseInt(value.slice(2, 4), 16),
    b: parseInt(value.slice(4, 6), 16),
  }
}

const rgbToHex = ({ r, g, b }) => `#${[r, g, b].map((value) => Math.round(value).toString(16).padStart(2, '0')).join('')}`

const mix = (color, target, weight) => {
  const source = hexToRgb(color)
  const targetRgb = hexToRgb(target)
  return rgbToHex({
    r: source.r * (1 - weight) + targetRgb.r * weight,
    g: source.g * (1 - weight) + targetRgb.g * weight,
    b: source.b * (1 - weight) + targetRgb.b * weight,
  })
}

export const useAppStore = defineStore('app', () => {
  const state = ref({ ...defaults, ...JSON.parse(localStorage.getItem(SETTINGS_KEY) || '{}') })
  const cachedViews = ref([])

  const collapsed = computed({
    get: () => state.value.collapsed,
    set: (value) => { state.value.collapsed = value },
  })

  const applyTheme = () => {
    const color = normalizeHex(state.value.primaryColor)
    state.value.primaryColor = color
    const root = document.documentElement

    root.classList.toggle('dark', state.value.dark)
    root.style.setProperty('--cot-primary', color)
    document.body.style.setProperty('--cot-primary', color)
    document.body.style.setProperty('--el-color-primary', color)
    document.getElementById('app')?.style.setProperty('--cot-primary', color)
    document.getElementById('app')?.style.setProperty('--el-color-primary', color)
    root.style.setProperty('--cot-primary-soft', state.value.dark ? `rgba(${hexToRgb(color).r}, ${hexToRgb(color).g}, ${hexToRgb(color).b}, 0.16)` : mix(color, '#ffffff', 0.88))
    root.style.setProperty('--el-color-primary', color)
    root.style.setProperty('--el-color-primary-dark-2', mix(color, '#000000', 0.18))

    for (let i = 1; i <= 9; i += 1) {
      root.style.setProperty(`--el-color-primary-light-${i}`, mix(color, '#ffffff', i / 10))
    }
  }

  const toggleDark = () => {
    state.value.dark = !state.value.dark
    applyTheme()
  }

  const setPrimaryColor = (color) => {
    state.value.primaryColor = normalizeHex(color)
    applyTheme()
  }

  const addCachedView = (name) => {
    if (state.value.cachePages && name && !cachedViews.value.includes(name)) cachedViews.value.push(name)
  }

  const removeCachedView = (name) => {
    cachedViews.value = cachedViews.value.filter((item) => item !== name)
  }

  watch(state, (value) => localStorage.setItem(SETTINGS_KEY, JSON.stringify(value)), { deep: true })

  return { state, collapsed, cachedViews, applyTheme, toggleDark, setPrimaryColor, addCachedView, removeCachedView }
})

