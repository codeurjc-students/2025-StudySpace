import { test, expect } from '@playwright/test';

test.describe('Statistics and Reservations Flow', () => {

  test('A user creates a reservation and the admin views the statistics.', async ({ page }) => {
    
    //data for test
    const timestamp = Date.now();
    const userEmail = `stats_user_${timestamp}@e2e.com`;
    const userPass = 'StatsPass123.';
    const userName = `Stats User ${timestamp}`;

    // avoid weekends
    const targetDate = new Date();
    targetDate.setDate(targetDate.getDate() + 3); //3 days to future
    while (targetDate.getDay() === 0 || targetDate.getDay() === 6) {
        targetDate.setDate(targetDate.getDate() + 1);
    }
    const dateStr = targetDate.toISOString().split('T')[0];

    //regsiter
    await test.step('User registration for statistics', async () => {
        await page.goto('/login');
        await page.getByRole('button', { name: 'Register' }).click();
        await page.getByLabel('Name').fill(userName);
        await page.getByLabel('Email').fill(userEmail);
        await page.locator('input[placeholder="Create a password"]').fill(userPass);

        const dialogPromise = page.waitForEvent('dialog');
        await page.getByRole('button', { name: 'Sign Up' }).click();
        const dialog = await dialogPromise;
        await dialog.dismiss();
        await expect(page).toHaveURL('/login');
    });

    // LOGIN AND RESERVATION
    await test.step('Usuario se loguea y reserva', async () => {
      await page.goto('/login'); 
      await page.getByPlaceholder('Email Address').fill(userEmail);
      await page.locator('input[placeholder="Enter password"]').fill(userPass);
      await page.getByRole('main').getByRole('button', { name: 'Log In' }).click();
      await expect(page).toHaveURL('/');

      await page.getByRole('button', { name: 'Book a room' }).click();
      
      //select room
      const roomSelect = page.locator('select[name="roomId"]');
      await expect(roomSelect).not.toBeDisabled();
      await roomSelect.selectOption({ index: 1 }); 

      //date
      await page.fill('input[name="selectedDate"]', dateStr);
      await page.locator('input[name="selectedDate"]').dispatchEvent('change');

      // hour
      const startSelect = page.locator('select[name="startTime"]');
      await expect(startSelect).toBeEnabled();
      
      //not first hour
      const optionsCount = await startSelect.locator('option').count();
      if (optionsCount > 3) {
          await startSelect.selectOption({ index: 3 }); 
      } else {
          await startSelect.selectOption({ index: 1 });
      }

      const endSelect = page.locator('select[name="endTime"]');
      await expect(endSelect).toBeEnabled();
      await page.waitForTimeout(200); 
      await endSelect.selectOption({ index: 1 });

      await page.locator('textarea[name="reason"]').fill(`Stats Check ${timestamp}`);
      await page.getByRole('button', { name: 'Confirm Reservation' }).click();
      
      await expect(page).toHaveURL('/');
    });

    // LOGOUT
    await test.step('Logout', async () => {
      await page.evaluate(() => { localStorage.clear(); sessionStorage.clear(); });
      await page.goto('/login');
    });

    // ADMIN VERIFICATION
    await test.step('Admin checks the statistics', async () => {
      await page.getByPlaceholder('Email Address').fill('admin@studyspace.com');
      await page.locator('input[placeholder="Enter password"]').fill('Admin12.');
      await page.getByRole('main').getByRole('button', { name: 'Log In' }).click();
      
      await expect(page).toHaveURL('/');
      await page.getByRole('button', { name: 'Admin Dashboard' }).click();
      
      await page.getByRole('button', { name: 'Occupancy Statistics' }).click();
      await expect(page).toHaveURL('/admin/stats');
      
      //filter start dtae
      await page.fill('#statsDate', dateStr);
      await page.locator('#statsDate').dispatchEvent('change');
      
      //graph
      await page.waitForTimeout(1000); 
      await expect(page.locator('canvas').first()).toBeVisible();
    });

  });
});