import { expect, test } from '@playwright/test';

test('영화를 찾아 좌석을 예약하고 모의 결제한 뒤 취소한다', async ({ page }) => {
  let orderStatus = 'READY';
  const startsAt = new Date(Date.now() + 48 * 60 * 60 * 1000).toISOString();
  const endsAt = new Date(Date.now() + 50 * 60 * 60 * 1000).toISOString();
  const screening = {
    id: 1, movieId: 1, movieTitle: '파도의 기록', theater: '1관',
    startsAt, endsAt,
    totalSeats: 2, availableSeats: 2,
  };
  const movie = {
    id: 1, title: '파도의 기록', genre: 'DRAMA', rating: 'R_12',
    runningTime: 120, poster: '', screenings: [screening],
  };
  await page.route('**/api/**', async route => {
    const url = new URL(route.request().url());
    const path = url.pathname;
    const method = route.request().method();
    let body: unknown = {};
    if (path === '/api/auth/csrf') body = 'test-token';
    else if (path === '/api/auth/me') body = { id: 7, email: 'guest@example.com', name: '관객' };
    else if (path === '/api/movies' && method === 'GET') body = { content: [movie], totalElements: 1, page: 0, size: 12 };
    else if (path === '/api/movies/1') body = movie;
    else if (path === '/api/screenings/1/seats') body = {
      screening, totalSeats: 2, availableSeats: 2,
      seats: [
        { id: 1, row: 1, col: 1, price: 12000, status: 'AVAILABLE' },
        { id: 2, row: 1, col: 2, price: 12000, status: 'AVAILABLE' },
      ],
    };
    else if (path === '/api/bookings' && method === 'POST') body = {
      orderId: 'order-1', orderName: '파도의 기록 2매', totalAmount: 24000,
      expiresAt: new Date(Date.now() + 5 * 60 * 1000).toISOString(),
    };
    else if (path.endsWith('/mock-payment/fail')) {
      await route.fulfill({ status: 409, contentType: 'application/json',
        body: JSON.stringify({ message: '모의 결제가 실패했습니다. 다시 시도할 수 있습니다.' }) });
      return;
    } else if (path.endsWith('/mock-payment')) {
      orderStatus = 'DONE'; body = { orderId: 'order-1', name: '파도의 기록 2매', amount: 24000, status: orderStatus, startsAt: screening.startsAt, seats: ['A1', 'A2'] };
    } else if (path.endsWith('/cancel')) {
      orderStatus = 'CANCELED'; body = { orderId: 'order-1', name: '파도의 기록 2매', amount: 24000, status: orderStatus, startsAt: screening.startsAt, seats: ['A1', 'A2'] };
    } else if (path === '/api/bookings' && method === 'GET') body = [{
      orderId: 'order-1', name: '파도의 기록 2매', amount: 24000,
      status: orderStatus, startsAt: screening.startsAt, seats: ['A1', 'A2'],
    }];
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(body) });
  });

  await page.goto('/');
  await expect(page.getByRole('button', { name: '공포' })).toBeVisible();
  await expect(page.getByRole('link', { name: /파도의 기록/ }).locator('img')).toHaveAttribute('src', '/posters/01.svg');
  await page.getByRole('link', { name: /파도의 기록/ }).click();
  await expect(page.getByTestId('movie-introduction')).toBeVisible();
  await expect(page.getByTestId('screening-date')).toHaveCount(1);
  await page.getByRole('button', { name: /1관/ }).click();
  await page.getByRole('button', { name: 'A1' }).click();
  await page.getByRole('button', { name: 'A2' }).click();
  await page.getByRole('button', { name: /예매하기/ }).click();
  await page.getByRole('button', { name: /결제 실패 체험/ }).click();
  await expect(page.getByText(/모의 결제가 실패했습니다/)).toBeVisible();
  await page.getByRole('button', { name: /모의 결제/ }).click();
  await expect(page.getByText('결제가 완료되었습니다')).toBeVisible();
  await page.getByRole('link', { name: '내 예매', exact: true }).click();
  await expect(page.getByText('A1, A2')).toBeVisible();
  page.once('dialog', dialog => dialog.accept());
  await page.getByRole('button', { name: /예매 취소/ }).click();
  await expect(page.getByText('취소 완료')).toBeVisible();
});
