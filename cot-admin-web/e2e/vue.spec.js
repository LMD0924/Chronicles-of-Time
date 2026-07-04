/**
 * 文件说明：拾光记后台管理系统拾光记项目脚本模块，封装拾光记项目相关的配置、状态、路由或工具逻辑。
 */
import { test, expect } from '@playwright/test'

test('visits the admin login page', async ({ page }) => {
  await page.goto('/login')
  await expect(page.locator('h2')).toHaveText('管理员登录')
})
