import { expect, test } from '@playwright/test';

test('좌석 갱신에서 점유된 선택 좌석을 해제한다', async ({ page }) => {
  let occupied = false;
  const screening = {
    id: 1, movieId: 1, movieTitle: '영화', theater: '1관',
    startsAt: new Date(Date.now() + 86_400_000).toISOString(),
    endsAt: new Date(Date.now() + 93_600_000).toISOString(),
    totalSeats: 1, availableSeats: 1,
  };
  await page.clock.install();
  await page.route('**/api/**', async route => {
    const path = new URL(route.request().url()).pathname;
    const body = path === '/api/auth/me'
      ? { id: 1, email: 'demo@example.com', name: '관객' }
      : { screening, totalSeats: 1, availableSeats: occupied ? 0 : 1,
          seats: [{ id: 1, row: 1, col: 1, price: 10000,
            status: occupied ? 'COMPLETED' : 'AVAILABLE' }] };
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(body) });
  });

  await page.goto('/screenings/1/seats');
  await page.getByRole('button', { name: 'A1' }).click();
  await expect(page.getByText('A1', { exact: true }).last()).toBeVisible();

  occupied = true;
  await page.clock.runFor(5_100);

  await expect(page.getByRole('button', { name: 'A1' })).toBeDisabled();
  await expect(page.getByText('선택 전')).toBeVisible();
  await expect(page.getByRole('button', { name: /예매하기/ })).toBeDisabled();
});

test('영화 목록 페이지를 바꿀 때 이전 페이지 결과를 숨긴다', async ({ page }) => {
  let releaseSecondPage: (() => void) | undefined;
  const movie = (id: number, title: string) => ({
    id, title, genre: 'DRAMA', rating: 'R_12', runningTime: 100,
    poster: '', screenings: [],
  });
  await page.route('**/api/**', async route => {
    const url = new URL(route.request().url());
    if (url.pathname === '/api/auth/me') {
      await route.fulfill({ status: 200, contentType: 'application/json', body: 'null' });
      return;
    }
    if (url.searchParams.get('page') === '1') {
      await new Promise<void>(resolve => { releaseSecondPage = resolve; });
    }
    const second = url.searchParams.get('page') === '1';
    await route.fulfill({ status: 200, contentType: 'application/json',
      body: JSON.stringify({ content: [second ? movie(2, '두 번째 영화') : movie(1, '첫 번째 영화')],
        totalElements: 13, page: second ? 1 : 0, size: 12 }) });
  });

  await page.goto('/');
  await expect(page.getByRole('link', { name: /첫 번째 영화/ })).toBeVisible();
  await page.getByRole('button', { name: '다음' }).click();
  await expect.poll(() => Boolean(releaseSecondPage)).toBe(true);
  await expect(page.getByText('영화를 불러오는 중입니다…')).toBeVisible();
  await expect(page.getByRole('link', { name: /첫 번째 영화/ })).toHaveCount(0);
  releaseSecondPage?.();
  await expect(page.getByRole('link', { name: /두 번째 영화/ })).toBeVisible();
});
