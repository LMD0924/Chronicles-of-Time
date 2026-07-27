/**
 * 文件说明：拾光记前台应用通用工具脚本模块，封装通用工具相关的配置、状态、路由或工具逻辑。
 */
export const ThemeType = {
  LIGHT: 'light',
  DARK: 'dark'
};

export const THEME_COLOR_PRESETS = [
  { key: 'mist-sand', name: '雾沙', primary: '#74858a', secondary: '#bd8e63', canvas: '#f2efe7', surface: '#fbfaf6', darkCanvas: '#151a1b', darkSurface: '#202829' },
  { key: 'polar-blue', name: '极夜冰蓝', primary: '#3d6f8f', secondary: '#9bc8cd', canvas: '#edf4f3', surface: '#fbfdfc', darkCanvas: '#091727', darkSurface: '#10243a' },
  { key: 'deep-teal', name: '深海青', primary: '#176b6a', secondary: '#3e7184', canvas: '#e9f1ef', surface: '#fbfdfc', darkCanvas: '#081d22', darkSurface: '#102a2d' },
  { key: 'noir-plum', name: '黑梅', primary: '#4c3154', secondary: '#9b8d99', canvas: '#f0eef0', surface: '#fbfafb', darkCanvas: '#151017', darkSurface: '#251c29' },
  { key: 'moonstone', name: '月岩', primary: '#425d6e', secondary: '#8a98a3', canvas: '#eef0ee', surface: '#fcfdfc', darkCanvas: '#0f1821', darkSurface: '#1a2731' },
  { key: 'dusk-rose', name: '暮色', primary: '#9a6263', secondary: '#4e7392', canvas: '#f4ebea', surface: '#fdfafa', darkCanvas: '#24223b', darkSurface: '#332a42' },
  { key: 'orchid-slate', name: '兰灰', primary: '#a97eaa', secondary: '#596a85', canvas: '#f5eff5', surface: '#fdfafd', darkCanvas: '#20233b', darkSurface: '#30344e' },
  { key: 'moss-field', name: '苔原', primary: '#3f704c', secondary: '#9ca071', canvas: '#f3f1de', surface: '#fcfcf6', darkCanvas: '#0c2119', darkSurface: '#1c3227' }
];

const THEME_STORAGE_KEY = 'app_theme';
const THEME_COLOR_STORAGE_KEY = 'app_theme_color';
const FONT_STORAGE_KEY = 'app_font';
const DEFAULT_THEME_COLOR = THEME_COLOR_PRESETS[0];

export const APP_FONT_PRESETS = [
  {
    key: 'system',
    name: '默认字体',
    description: '适合长时间阅读和表单操作。'
  },
  {
    key: 'lawyer-handwriting',
    name: '黄楷律师手写体',
    description: '更有手写质感，适合标题、记录和个人化界面。'
  },
  {
    key: 'honglei-xingshu',
    name: '鸿雷行书简体',
    description: '行书风格更舒展，适合展示标题和故事型页面。'
  },
  {
    key: 'yezi-xiaoshitou',
    name: '叶子工厂小石头',
    description: '更轻松活泼，适合成长记录和卡片式内容。'
  },
  {
    key: 'yunfeng-hanchan',
    name: '云峰寒蝉体',
    description: '笔画更有古风气质，适合寄语、相册和时间线内容。'
  }
];

const DEFAULT_APP_FONT = APP_FONT_PRESETS[0];
const SHADE_STEPS = [50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950];

const isBrowser = () => typeof window !== 'undefined' && typeof document !== 'undefined';

const normalizeHex = (value, fallback = DEFAULT_THEME_COLOR.primary) => {
  if (typeof value !== 'string') return fallback;
  const raw = value.trim();
  const hex = raw.startsWith('#') ? raw : `#${raw}`;

  if (/^#[0-9a-fA-F]{6}$/.test(hex)) return hex.toLowerCase();
  if (/^#[0-9a-fA-F]{3}$/.test(hex)) {
    return `#${hex[1]}${hex[1]}${hex[2]}${hex[2]}${hex[3]}${hex[3]}`.toLowerCase();
  }
  return fallback;
};

const hexToRgb = (hex) => {
  const normalized = normalizeHex(hex);
  return {
    r: parseInt(normalized.slice(1, 3), 16),
    g: parseInt(normalized.slice(3, 5), 16),
    b: parseInt(normalized.slice(5, 7), 16)
  };
};

const mix = (from, to, amount) => ({
  r: Math.round(from.r * (1 - amount) + to.r * amount),
  g: Math.round(from.g * (1 - amount) + to.g * amount),
  b: Math.round(from.b * (1 - amount) + to.b * amount)
});

const rgbChannels = ({ r, g, b }) => `${r} ${g} ${b}`;

const createPalette = (hex) => {
  const base = hexToRgb(hex);
  const white = { r: 255, g: 255, b: 255 };
  const black = { r: 15, g: 23, b: 42 };

  return {
    50: rgbChannels(mix(base, white, 0.92)),
    100: rgbChannels(mix(base, white, 0.84)),
    200: rgbChannels(mix(base, white, 0.68)),
    300: rgbChannels(mix(base, white, 0.48)),
    400: rgbChannels(mix(base, white, 0.22)),
    500: rgbChannels(base),
    600: rgbChannels(mix(base, black, 0.12)),
    700: rgbChannels(mix(base, black, 0.26)),
    800: rgbChannels(mix(base, black, 0.42)),
    900: rgbChannels(mix(base, black, 0.58)),
    950: rgbChannels(mix(base, black, 0.74))
  };
};

const getPresetByKey = (key) => {
  return THEME_COLOR_PRESETS.find((preset) => preset.key === key) || DEFAULT_THEME_COLOR;
};

const normalizeThemeColor = (value) => {
  const key = typeof value === 'string' ? value : value?.preset || value?.key;
  const preset = getPresetByKey(key);
  return { ...preset, preset: preset.key };
};

const normalizeFont = (value) => {
  const key = typeof value === 'string' ? value : value?.key;
  return APP_FONT_PRESETS.find((preset) => preset.key === key) || DEFAULT_APP_FONT;
};

const setPaletteVariables = (prefix, palette) => {
  const root = document.documentElement;
  SHADE_STEPS.forEach((shade) => {
    root.style.setProperty(`--color-${prefix}-${shade}`, palette[shade]);
  });
};

export const getStoredTheme = () => {
  const stored = isBrowser() ? localStorage.getItem(THEME_STORAGE_KEY) : null;
  if (stored === ThemeType.LIGHT || stored === ThemeType.DARK) {
    return stored;
  }

  const isSystemDark = isBrowser() && window.matchMedia('(prefers-color-scheme: dark)').matches;
  return isSystemDark ? ThemeType.DARK : ThemeType.LIGHT;
};

export const getStoredThemeColor = () => {
  if (!isBrowser()) return { ...DEFAULT_THEME_COLOR, preset: DEFAULT_THEME_COLOR.key };

  try {
    const stored = localStorage.getItem(THEME_COLOR_STORAGE_KEY);
    if (!stored) return { ...DEFAULT_THEME_COLOR, preset: DEFAULT_THEME_COLOR.key };
    return normalizeThemeColor(JSON.parse(stored));
  } catch (_) {
    return { ...DEFAULT_THEME_COLOR, preset: DEFAULT_THEME_COLOR.key };
  }
};

export const getStoredFont = () => {
  if (!isBrowser()) return DEFAULT_APP_FONT;
  return normalizeFont(localStorage.getItem(FONT_STORAGE_KEY));
};

export const applyFont = (font = getStoredFont()) => {
  const normalized = normalizeFont(font);
  if (!isBrowser()) return normalized;

  document.documentElement.dataset.appFont = normalized.key;
  return normalized;
};

export const setFont = (font) => {
  const normalized = applyFont(font);
  localStorage.setItem(FONT_STORAGE_KEY, normalized.key);

  window.dispatchEvent(new CustomEvent('font-change', {
    detail: normalized,
    bubbles: false,
    cancelable: false
  }));

  return normalized;
};

export const applyThemeColor = (themeColor = getStoredThemeColor()) => {
  if (!isBrowser()) return themeColor;

  const normalized = normalizeThemeColor(themeColor);
  const root = document.documentElement;
  const primaryPalette = createPalette(normalized.primary);
  const secondaryPalette = createPalette(normalized.secondary);
  const primaryRgb = rgbChannels(hexToRgb(normalized.primary));
  const secondaryRgb = rgbChannels(hexToRgb(normalized.secondary));

  root.dataset.appTheme = normalized.key;
  root.style.setProperty('--app-light-canvas', normalized.canvas);
  root.style.setProperty('--app-light-surface', normalized.surface);
  root.style.setProperty('--app-dark-canvas', normalized.darkCanvas);
  root.style.setProperty('--app-dark-surface', normalized.darkSurface);

  root.style.setProperty('--theme-primary', normalized.primary);
  root.style.setProperty('--theme-secondary', normalized.secondary);
  root.style.setProperty('--theme-primary-rgb', primaryRgb);
  root.style.setProperty('--theme-secondary-rgb', secondaryRgb);
  root.style.setProperty('--theme-gradient', `linear-gradient(135deg, ${normalized.primary} 0%, ${normalized.secondary} 100%)`);
  root.style.setProperty('--theme-gradient-soft', `linear-gradient(135deg, rgba(${primaryRgb}, 0.14), rgba(${secondaryRgb}, 0.14))`);

  setPaletteVariables('brand', primaryPalette);
  setPaletteVariables('accent', secondaryPalette);

  root.style.setProperty('--el-color-primary', normalized.primary);
  root.style.setProperty('--el-color-primary-dark-2', normalized.secondary);
  root.style.setProperty('--ant-color-primary', normalized.primary);

  return normalized;
};

export const setThemeColor = (themeColor) => {
  const normalized = applyThemeColor(themeColor);
  localStorage.setItem(THEME_COLOR_STORAGE_KEY, JSON.stringify(normalized));

  window.dispatchEvent(new CustomEvent('theme-color-change', {
    detail: normalized,
    bubbles: false,
    cancelable: false
  }));

  return normalized;
};

export const resetThemeColor = () => setThemeColor(DEFAULT_THEME_COLOR.key);

export const setTheme = (theme) => {
  document.documentElement.classList.toggle('dark', theme === ThemeType.DARK);
  document.documentElement.classList.toggle('light', theme === ThemeType.LIGHT);
  localStorage.setItem(THEME_STORAGE_KEY, theme);
  applyThemeColor(getStoredThemeColor());

  window.dispatchEvent(new CustomEvent('theme-change', {
    detail: theme,
    bubbles: false,
    cancelable: false
  }));
};

export const toggleTheme = () => {
  const current = getStoredTheme();
  const newTheme = current === ThemeType.DARK ? ThemeType.LIGHT : ThemeType.DARK;
  setTheme(newTheme);
  return newTheme;
};

export const initTheme = () => {
  applyThemeColor(getStoredThemeColor());
  applyFont(getStoredFont());
  setTheme(getStoredTheme());
};

export const onThemeChange = (callback) => {
  callback(getStoredTheme());

  const handler = (e) => callback(e.detail);
  window.addEventListener('theme-change', handler);

  return () => window.removeEventListener('theme-change', handler);
};

export const onThemeColorChange = (callback) => {
  callback(getStoredThemeColor());

  const handler = (e) => callback(e.detail);
  window.addEventListener('theme-color-change', handler);

  return () => window.removeEventListener('theme-color-change', handler);
};

export const onFontChange = (callback) => {
  callback(getStoredFont());

  const handler = (e) => callback(e.detail);
  window.addEventListener('font-change', handler);

  return () => window.removeEventListener('font-change', handler);
};