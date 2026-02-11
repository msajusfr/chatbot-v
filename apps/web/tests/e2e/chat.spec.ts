import { test, expect } from '@playwright/test';

test('home displays chat shell', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'chatbot-v' })).toBeVisible();
});
