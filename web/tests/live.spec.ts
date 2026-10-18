import { expect, test } from '@playwright/test';

test('Docker 서비스에서 가입부터 취소까지 이어진다', async ({ page }) => {
  test.skip(!process.env.LIVE_BASE_URL, 'Docker 서비스 검증 시에만 실행');
  await page.goto(process.env.LIVE_BASE_URL!);
  await page.getByRole('link', { name: '로그인' }).click();
  await page.getByRole('button', { name: /회원가입/ }).click();
  await page.getByRole('textbox', { name: '이름' }).fill('로컬관객');
  await page.getByRole('textbox', { name: '이메일' }).fill(`test-${Date.now()}@example.com`);
  await page.getByLabel('비밀번호').fill('password123');
  await page.getByRole('button', { name: '계정 만들기' }).click();
  await expect(page.getByText('로컬관객님')).toBeVisible();
  await expect.poll(() => page.locator('.movie-card img').first().evaluate(image => (image as HTMLImageElement).naturalWidth)).toBeGreaterThan(0);
  await page.locator('.movie-card').first().click();
  await page.locator('.screening-item').first().click();
  await page.locator('.seat:not([disabled])').first().click();
  await page.getByRole('button', { name: /예매하기/ }).click();
  await page.getByRole('button', { name: /모의 결제/ }).click();
  await expect(page.getByText('결제가 완료되었습니다')).toBeVisible();
  await page.getByRole('link', { name: '내 예매', exact: true }).click();
  await expect(page.locator('.order-card').first()).toContainText('1매');
  page.once('dialog', dialog => dialog.accept());
  await page.getByRole('button', { name: '예매 취소' }).click();
  await expect(page.getByText('취소 완료')).toBeVisible();
  await page.goto(process.env.LIVE_BASE_URL!);
  await page.locator('.movie-card').first().click();
  await page.locator('.screening-item').first().click();
  await expect(page.locator('.seat:not([disabled])').first()).toBeVisible();
});

test('모바일에서 영화와 좌석 화면을 사용할 수 있다', async ({ page }) => {
  test.skip(!process.env.LIVE_BASE_URL, 'Docker 서비스 검증 시에만 실행');
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto(process.env.LIVE_BASE_URL!);
  await expect(page.locator('.movie-card').first()).toBeVisible();
  await page.locator('.movie-card').first().click();
  await page.locator('.screening-item').first().click();
  await expect(page.locator('.seat-map')).toBeVisible();
  await expect(page.locator('.booking-summary')).toBeVisible();
});
