export const ThemeType = {
  LIGHT: 'light',
  DARK: 'dark'
};

// 主题存储键名
const THEME_STORAGE_KEY = 'app_theme';

// 1. 获取本地存储的主题（无则自动检测系统主题）
export const getStoredTheme = () => {
  // 优先读取本地存储
  const stored = localStorage.getItem(THEME_STORAGE_KEY);
  if (stored === ThemeType.LIGHT || stored === ThemeType.DARK) {
    return stored;
  }

  // 自动检测系统主题
  const isSystemDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  return isSystemDark ? ThemeType.DARK : ThemeType.LIGHT;
};

// 2. 设置主题（同步到DOM + 本地存储）
export const setTheme = (theme) => {
  // 更新HTML根元素类名
  document.documentElement.classList.toggle('dark', theme === ThemeType.DARK);
  document.documentElement.classList.toggle('light', theme === ThemeType.LIGHT);

  // 存储到本地
  localStorage.setItem(THEME_STORAGE_KEY, theme);

  // 触发全局自定义事件（供所有页面监听）
  // 兼容旧版浏览器的 CustomEvent 构造函数
  const event = new CustomEvent('theme-change', {
    detail: theme,
    bubbles: false,
    cancelable: false
  });
  window.dispatchEvent(event);
};

// 3. 切换主题（核心方法）
export const toggleTheme = () => {
  const current = getStoredTheme();
  const newTheme = current === ThemeType.DARK ? ThemeType.LIGHT : ThemeType.DARK;
  setTheme(newTheme);
  return newTheme;
};

// 4. 初始化主题（应用启动时调用）
export const initTheme = () => {
  setTheme(getStoredTheme());
};

// 5. 监听主题变化（供组件使用）
export const onThemeChange = (callback) => {
  // 初始触发一次
  callback(getStoredTheme());

  // 监听自定义事件
  const handler = (e) => callback(e.detail);
  window.addEventListener('theme-change', handler);

  // 返回取消监听函数
  return () => window.removeEventListener('theme-change', handler);
};
