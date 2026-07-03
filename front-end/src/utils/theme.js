/**
 * 文件说明：拾光记前台应用通用工具脚本模块，封装通用工具相关的配置、状态、路由或工具逻辑。
 */
export const ThemeType = {
  LIGHT: 'light',
  DARK: 'dark'
};

export const THEME_COLOR_PRESETS = [
  { key: 'pink-purple', name: '粉紫', primary: '#d946ef', secondary: '#8b5cf6' },
  { key: 'rose-violet', name: '玫紫', primary: '#e11d48', secondary: '#7c3aed' },
  { key: 'cyan-purple', name: '青紫', primary: '#06b6d4', secondary: '#9333ea' },
  { key: 'orange-pink', name: '橙粉', primary: '#f97316', secondary: '#ec4899' },
  { key: 'blue-violet', name: '蓝紫', primary: '#2563eb', secondary: '#8b5cf6' }
];

const THEME_STORAGE_KEY = 'app_theme';
const THEME_COLOR_STORAGE_KEY = 'app_theme_color';
const DEFAULT_THEME_COLOR = THEME_COLOR_PRESETS[0];
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
  if (typeof value === 'string') {
    const preset = getPresetByKey(value);
    return { ...preset };
  }

  const preset = value?.preset ? getPresetByKey(value.preset) : DEFAULT_THEME_COLOR;
  const primary = normalizeHex(value?.primary, preset.primary);
  const secondary = normalizeHex(value?.secondary, preset.secondary);

  return {
    key: value?.preset || preset.key,
    name: value?.name || preset.name,
    preset: value?.preset || preset.key,
    primary,
    secondary
  };
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

export const applyThemeColor = (themeColor = getStoredThemeColor()) => {
  if (!isBrowser()) return themeColor;

  const normalized = normalizeThemeColor(themeColor);
  const root = document.documentElement;
  const primaryPalette = createPalette(normalized.primary);
  const secondaryPalette = createPalette(normalized.secondary);
  const primaryRgb = rgbChannels(hexToRgb(normalized.primary));
  const secondaryRgb = rgbChannels(hexToRgb(normalized.secondary));

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
