import { test, expect } from '@playwright/test';

test.describe('User Reservation Management by Admin', () => {

  test.setTimeout(120000);

  test('Admin should be able to see a specific users bookings', async ({ page }) => {
    
    const timestamp = Date.now();
    const uniqueReason = 'Reunión E2E ' + timestamp;
    const uniqueUserEmail = `user_${timestamp}@e2e.test`;
    const uniqueUserPass = 'TestPass123.';

    // --- avoid saturday and sunday ---
    const targetDate = new Date();
    targetDate.setDate(targetDate.getDate() + 2); 
    while (targetDate.getDay() === 0 || targetDate.getDay() === 6) {
        targetDate.setDate(targetDate.getDate() + 1);
    }
    const dateStr = targetDate.toISOString().split('T')[0];
    // ------------------------------------------------

    // ==========================================
    // NEW USER REGISTRATION
    // ==========================================
    await test.step('Registrar usuario nuevo', async () => {
      await page.goto('/login');
      await page.getByRole('button', { name: 'Register' }).click();
      
      await page.getByLabel('Name').fill('User E2E Temp');
      await page.getByLabel('Email').fill(uniqueUserEmail);
      await page.locator('input[placeholder="Create a password"]').fill(uniqueUserPass);

      const dialogPromise = page.waitForEvent('dialog');
      await page.getByRole('button', { name: 'Sign Up' }).click();
      const dialog = await dialogPromise;
      await dialog.dismiss();

      await expect(page).toHaveURL('/login');
    });

    // ==========================================
    // LOGIN & RESERVATION (USER)
    // ==========================================
    await test.step('User creates a reservation', async () => {
      await page.goto('/login');
      
      await page.getByPlaceholder('Email Address').fill(uniqueUserEmail);
      await page.locator('input[placeholder="Enter password"]').fill(uniqueUserPass);
      await page.getByRole('main').getByRole('button', { name: 'Log In' }).click();
      await expect(page).toHaveURL('/');

      //create reservation
      await page.getByRole('button', { name: 'Book a room' }).click();
      
      const roomSelect = page.locator('select[name="roomId"]');
      await expect(roomSelect).not.toBeDisabled();
      await roomSelect.selectOption({ index: 1 });

      await page.fill('input[name="selectedDate"]', dateStr);
      await page.locator('input[name="selectedDate"]').dispatchEvent('change');

      const startSelect = page.locator('select[name="startTime"]');
        await expect(startSelect).toBeEnabled();
        await expect(startSelect.locator('option')).not.toHaveCount(0);
        
        // second available option and index 1 for fist avaliable (0 will be select, not the option)
        await startSelect.selectOption({ index: 1 });

        const endSelect = page.locator('select[name="endTime"]');
        await expect(endSelect).toBeEnabled();
        await page.waitForTimeout(500); 
        
        await endSelect.selectOption({ index: 1 });

        await page.locator('textarea[name="reason"]').fill(uniqueReason);
        await page.getByRole('button', { name: 'Confirm Reservation' }).click();
        await expect(page).toHaveURL('/', { timeout: 10000 });
    });

    // ==========================================
    // 3. LOGOUT
    // ==========================================
    await test.step('User logout', async () => {
      await page.evaluate(() => {
          localStorage.clear();
          sessionStorage.clear();
      });
      await page.goto('/login');
      await expect(page).toHaveURL(/\/login/);
    });

    // ==========================================
    // ADMIN QUERY
    // ==========================================
    await test.step('Admin searches for the user and views their bookings', async () => {
      await page.getByPlaceholder('Email Address').fill('admin@studyspace.com');
      await page.locator('input[placeholder="Enter password"]').fill('Admin12.');
      await page.getByRole('main').getByRole('button', { name: 'Log In' }).click();
      
      await page.getByRole('button', { name: 'Admin Dashboard' }).click();
      await page.getByRole('button', { name: 'Manage Users' }).click();
      
      // ROBUST SEARCH FUNCTION
      const findUserRobustly = async (email: string) => {
          const row = page.getByRole('row').filter({ hasText: email });
          const pageIndicator = page.locator('small', { hasText: /Showing page/ });

          // wait till at least 1 row on the table is visible
          await expect(page.locator('tbody tr').first()).toBeVisible({ timeout: 10000 });

          while (true) {
              try {
                  await expect(row).toBeVisible({ timeout: 5000 });
                  return true; //if found
              } catch (e) {
                  //maybe next page
              }

              const nextBtn = page.getByRole('button', { name: '»', exact: true });

              //is there is still the button
              const isNextDisabled = await nextBtn.isDisabled() || 
                                     await page.locator('li.page-item.disabled button', { hasText: '»' }).count() > 0;

              //if no button end reached
              if (!await nextBtn.isVisible() || isNextDisabled) {
                  return false; 
              }

              const currentText = await pageIndicator.textContent();
              
              await nextBtn.click({ force: true });

              // wait till page changes
              await expect(pageIndicator).not.toHaveText(currentText!, { timeout: 5000 });
          }
      };

      const found = await findUserRobustly(uniqueUserEmail);
      expect(found, `The user ${uniqueUserEmail} did not appear on any page`).toBeTruthy();

      const userRow = page.getByRole('row').filter({ hasText: uniqueUserEmail });
      
      await userRow.getByRole('button', { name: /Books|📅/ }).click();

      const reservationReason = page.getByText(uniqueReason);
      await expect(reservationReason).toBeVisible();

      // ==========================================
      // CLEANUP 
      // ==========================================
      await test.step('Cleanup: Delete the test user', async () => {
          await page.getByRole('button', { name: /Back to Users/i }).click().catch(() => {
             console.log("The back button could not be clicked, forcing navigation...");
          });
          
          if (!page.url().includes('/admin/users')) {
             await page.getByRole('button', { name: 'Manage Users' }).click();
          }

          const foundAgain = await findUserRobustly(uniqueUserEmail);
          
          if (foundAgain) {
              const deleteRow = page.getByRole('row').filter({ hasText: uniqueUserEmail });
              
              // acept confirm on navigation
              page.on('dialog', dialog => dialog.accept());
              
              await deleteRow.getByRole('button', { name: /Delete|🗑️/ }).click();
              
              await expect(deleteRow).not.toBeVisible();
          }
      });


    });

  });
});