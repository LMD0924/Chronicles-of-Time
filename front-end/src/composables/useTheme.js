import { ref, onMounted, onUnmounted } from 'vue'
import {
  getStoredTheme,
  getStoredThemeColor,
  onThemeChange,
  onThemeColorChange,
  ThemeType
} from '@/utils/theme'

/** 全局明暗主题和品牌色，与 theme.js / CSS 变量同步 */
export function useTheme() {
  const isDark = ref(getStoredTheme() === ThemeType.DARK)
  const themeColor = ref(getStoredThemeColor())
  let stopThemeListen = null
  let stopColorListen = null

  onMounted(() => {
    stopThemeListen = onThemeChange((theme) => {
      isDark.value = theme === ThemeType.DARK
    })
    stopColorListen = onThemeColorChange((color) => {
      themeColor.value = color
    })
  })

  onUnmounted(() => {
    stopThemeListen?.()
    stopColorListen?.()
  })

  return { isDark, themeColor, ThemeType }
}
