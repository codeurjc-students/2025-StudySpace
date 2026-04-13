import { test, expect } from '@playwright/test';

test.describe('Statistics and Reservations Flow', () => {
  test('A user creates a reservation and the admin views the statistics.', async ({
    page,
    request,
  }) => {
    //data for test
    const timestamp = Date.now();
    const userEmail = `stats_user_${timestamp}@e2e.com`;
    const userPass = 'StatsPass123.';
    const userName = `Stats User ${timestamp}`;

    // avoid weekends
    const targetDate = new Date();
    targetDate.setDate(targetDate.getDate() + 10);
    while (targetDate.getDay() === 0 || targetDate.getDay() === 6) {
      targetDate.setDate(targetDate.getDate() + 1);
    }
    const year = targetDate.getFullYear();
    const month = String(targetDate.getMonth() + 1).padStart(2, '0');
    const day = String(targetDate.getDate()).padStart(2, '0');
    const dateStr = `${year}-${month}-${day}`;

    //regsiter
    await test.step('User registration for statistics', async () => {
      await page.goto('/login');
      await page.getByRole('button', { name: 'Register' }).click();
      await page.getByLabel('Name').fill(userName);
      await page.getByLabel('Email').fill(userEmail);
      await page
        .locator('input[placeholder="Create a password"]')
        .fill(userPass);

      await page.getByRole('button', { name: 'Sign Up' }).click();
      const modal = page.getByRole('dialog');
      await expect(modal).toBeVisible();
      await modal.getByRole('button', { name: 'OK' }).click();
    });

    await test.step('Verify email via MailHog', async () => {
      let message = null;
      for (let i = 0; i < 15; i++) {
        try {
          const response = await request.get(
            'http://127.0.0.1:8025/api/v2/messages',
          );
          if (response.ok()) {
            const emailData = await response.json();
            const found = emailData.items.find(
              (msg: any) =>
                msg.Content.Headers.To &&
                msg.Content.Headers.To[0].includes(userEmail),
            );
            if (found) {
              message = found;
              break;
            }
          }
        } catch (e) {}
        await page.waitForTimeout(1000);
      }
      expect(message, 'The verification email was not found').toBeTruthy();
      const cleanBody = message.Content.Body.replace(/=\r?\n/g, '');
      const match = cleanBody.match(/token=([a-zA-Z0-9-]+)/);
      await page.goto(`/verify-email?token=${match![1]}`);
      await page.waitForTimeout(2000);
    });

    // LOGIN AND RESERVATION
    await test.step('Usuario se loguea y reserva', async () => {
      await page.goto('/login');
      await page.getByPlaceholder('Email Address').fill(userEmail);
      await page.locator('input[placeholder="Enter password"]').fill(userPass);
      await page
        .getByRole('main')
        .getByRole('button', { name: 'Log In' })
        .click();
      await expect(page).toHaveURL('/');

      await page.getByRole('button', { name: /Book a room/i }).click();

      const firstRoomCard = page
        .locator('div.list-group button.list-group-item')
        .first();
      await expect(firstRoomCard).toBeVisible();
      await firstRoomCard.click();
      await page.waitForTimeout(1000);

      const dateInput = page.getByLabel('2. Select Date');
      await dateInput.click();
      await dateInput.fill(dateStr);
      await dateInput.press('Tab');
      await page.waitForTimeout(3000);

      const startSelect = page.locator('select[name="startTime"]');
      await startSelect.selectOption({ index: 1 });
      await page.waitForTimeout(500);

      const endSelect = page.locator('select[name="endTime"]');
      await endSelect.selectOption({ index: 1 });

      await page
        .locator('textarea[name="reason"]')
        .fill(`Stats Check ${timestamp}`);

      await page.getByRole('button', { name: 'Confirm Reservation' }).click();
      const confirmModal = page.getByRole('dialog');
      await expect(confirmModal).toBeVisible();
      await confirmModal.getByRole('button', { name: 'OK' }).click();

      await expect(page).toHaveURL('/');
    });

    // LOGOUT
    await test.step('Logout', async () => {
      if (!(await page.getByRole('button', { name: 'Log Out' }).isVisible())) {
        await page.locator('#dropdownProfile').click();
      }
      await page.getByRole('button', { name: 'Log Out' }).click();
    });

    // ADMIN VERIFICATION
    await test.step('Admin checks the statistics', async () => {
      await page.goto('/login');
      await page
        .getByPlaceholder('Email Address')
        .fill('studyspacetfg@gmail.com');
      await page
        .locator('input[placeholder="Enter password"]')
        .fill('Admin12.');
      await page
        .getByRole('main')
        .getByRole('button', { name: 'Log In' })
        .click();

      await expect(page).toHaveURL('/');
      await page.getByRole('button', { name: /Admin Panel/i }).click();
      await page.getByRole('button', { name: /Occupancy Stats/i }).click();
      await expect(page).toHaveURL('/admin/stats');

      await page.fill('#statsDate', dateStr);
      await page.locator('#statsDate').dispatchEvent('change');

      //graph
      await page.waitForTimeout(1000);
      await expect(page.locator('canvas').first()).toBeVisible();
    });
  });
});
