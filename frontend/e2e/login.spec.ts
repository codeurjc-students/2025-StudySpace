import { test, expect } from '@playwright/test';

test.describe('User Authentication (Dynamic Data)', () => {

  test('It should allow login with newly created credentials and display the profile.', async ({ page }) => {
    
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
        await page.locator('input[placeholder="Create a password"]').fill(password);

        const dialogPromise = page.waitForEvent('dialog');
        await page.getByRole('button', { name: 'Sign Up' }).click();
        const dialog = await dialogPromise;
        await dialog.dismiss();
        
        await expect(page).toHaveURL('/login');
    });

    //  TEST LOGIN 
    await test.step('Login with the created credentials', async () => {
        await page.getByPlaceholder('Email Address').fill(email);
        await page.locator('input[placeholder="Enter password"]').fill(password);
        
        await page.getByRole('main').getByRole('button', { name: 'Log In', exact: true }).click();

        // Verify
        await expect(page).toHaveURL('/');
        await expect(page.getByRole('button', { name: 'Log In' }).first()).not.toBeVisible();

        const profileDropdown = page.locator('#dropdownProfile');
        await expect(profileDropdown).toBeVisible();
        await expect(profileDropdown).toContainText(name);
    });
  });

  test('It should display an error with incorrect credentials.', async ({ page }) => {
      await page.goto('/login');
      await expect(page.getByRole('heading', { name: 'Please Log In' })).toBeVisible();
 
      await page.getByPlaceholder('Email Address').fill('no_existe@test.com');
      await page.locator('input[placeholder="Enter password"]').fill('wrongpass');
      
      await page.getByRole('main').getByRole('button', { name: 'Log In', exact: true }).click();
      
      const errorDialog = page.getByRole('dialog');
      await expect(errorDialog).toBeVisible({ timeout: 10000 });
      
      await expect(errorDialog.getByText(/Incorrect username or password/)).toBeVisible();
      
      await errorDialog.getByRole('button', { name: 'Close' }).first().click();

      await expect(page).toHaveURL('/login');
    });
});