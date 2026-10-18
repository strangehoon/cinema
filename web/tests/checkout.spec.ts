import { expect, test } from '@playwright/test';

test('결제 대기 주문의 남은 시간과 만료 상태를 보여준다', async ({ page }) => {
  const expiresAt = new Date(Date.now() + 120_000).toISOString();
  await page.route('**/api/**', async route => {
    const path = new URL(route.request().url()).pathname;
    const body = path === '/api/auth/me'
      ? { id: 1, email: 'demo@example.com', name: '관객' }
      : path === '/api/bookings/order-1'
        ? { orderId: 'order-1', name: '영화 1매', amount: 10000, status: 'READY', expiresAt, seats: ['A1'] }
        : 'test-token';
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(body) });
  });

  await page.goto('/checkout/order-1');
  await expect(page.getByTestId('hold-countdown')).toContainText(/1:|2:/);
  await page.clock.install({ time: new Date(expiresAt) });
  await expect(page.getByTestId('hold-countdown')).toContainText('0:00');
  await expect(page.getByRole('button', { name: /모의 결제 완료하기/ })).toBeDisabled();
});

test('토스 설정이면 테스트 카드 결제를 선택할 수 있다', async ({ page }) => {
  await page.route('**/api/**', async route => {
    const path = new URL(route.request().url()).pathname;
    const body = path === '/api/auth/me'
      ? { id: 1, email: 'demo@example.com', name: '관객' }
      : path === '/api/bookings/payment-config'
        ? { mode: 'toss', clientKey: 'test_ck_sample' }
        : { orderId: 'order-1', name: '영화 1매', amount: 10000, status: 'READY',
            expiresAt: new Date(Date.now() + 120_000).toISOString(), seats: ['A1'] };
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(body) });
  });

  await page.goto('/checkout/order-1');
  await expect(page.getByRole('button', { name: /토스 테스트 카드로 결제/ })).toBeVisible();
});
