import { test, expect } from '@playwright/test'

test('visits the admin login page', async ({ page }) => {
  await page.goto('/login')
  await expect(page.locator('h2')).toHaveText('管理员登录')
})
