import { test, expect } from '@playwright/test';

test.describe('User Authentication (Dynamic Data)', () => {
  test('It should allow login with newly created credentials and display the profile.', async ({
    page,
    request,
  }) => {
    //new data for test
    const timestamp = Date.now();
    const email = `login_test_${timestamp}@e2e.com`;
    const password = 'PassLogin123.';
    const name = `Login User ${timestamp}`;

    //register to get sure user exists on back
    await test.step('Register seed user', async () => {
      await page.goto('/login');
      await page.getByRole('button', { name: 'Register' }).click();

      await page.getByLabel('Name').fill(name);
      await page.getByLabel('Email').fill(email);
      await page
        .locator('input[placeholder="Create a password"]')
        .fill(password);

      await page.getByRole('button', { name: 'Sign Up' }).click();
      const dialog = page.getByRole('dialog');
      await expect(dialog).toBeVisible();
      await dialog.getByRole('button', { name: 'OK' }).click();

      await expect(page).toHaveURL('/login');
    });

    //check mailhog
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
                msg.Content.Headers.To[0].includes(email),
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
      expect(match).toBeTruthy();

      await page.goto(`/verify-email?token=${match![1]}`);
      await page.waitForTimeout(2000);
      await page.goto('/login');
    });

    //  TEST LOGIN
    await test.step('Login with the created credentials', async () => {
      await page.getByPlaceholder('Email Address').fill(email);
      await page.locator('input[placeholder="Enter password"]').fill(password);

      await page
        .getByRole('main')
        .getByRole('button', { name: 'Log In', exact: true })
        .click();

      // Verify
      await expect(page).toHaveURL('/');

      const profileDropdown = page.locator('#dropdownProfile');
      await expect(profileDropdown).toBeVisible();
      await expect(profileDropdown).toContainText(name);
    });
  });

  test('It should display an error with incorrect credentials.', async ({
    page,
  }) => {
    await page.goto('/login');
    await expect(
      page.getByRole('heading', { name: 'Please Log In' }),
    ).toBeVisible();

    await page.getByPlaceholder('Email Address').fill('no_existe@test.com');
    await page.locator('input[placeholder="Enter password"]').fill('wrongpass');

    await page
      .getByRole('main')
      .getByRole('button', { name: 'Log In', exact: true })
      .click();

    const dialog = page.getByRole('dialog');
    await expect(dialog).toBeVisible({ timeout: 15000 });
    await expect(
      dialog.getByText(/Incorrect username or password/i),
    ).toBeVisible();

    await dialog.getByRole('button', { name: 'Close' }).click();
    await expect(page).toHaveURL('/login');
  });
});
