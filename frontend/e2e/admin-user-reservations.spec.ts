import { test, expect } from '@playwright/test';

test.describe('Gestión de Reservas de Usuarios por Admin', () => {

  test.setTimeout(60000);

  test('Admin debe poder ver las reservas de un usuario específico', async ({ page }) => {
    
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
    await test.step('Usuario crea una reserva', async () => {
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
      
      //whait for operations
      await expect(startSelect.locator('option')).toHaveCount(await startSelect.locator('option').count());
      
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

      await page.locator('textarea[name="reason"]').fill(uniqueReason);

      await page.getByRole('button', { name: 'Confirm Reservation' }).click();
      
      await expect(page).toHaveURL('/');
    });

    // ==========================================
    // 3. LOGOUT
    // ==========================================
    await test.step('Logout del usuario', async () => {
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
    await test.step('Admin busca al usuario y ve sus reservas', async () => {
      await page.getByPlaceholder('Email Address').fill('admin@studyspace.com');
      await page.locator('input[placeholder="Enter password"]').fill('Admin12.');
      await page.getByRole('main').getByRole('button', { name: 'Log In' }).click();
      
      await page.getByRole('button', { name: 'Admin Dashboard' }).click();
      await page.getByRole('button', { name: 'Manage Users' }).click();
      
      // ROBUST SEARCH FUNCTION
      const findUserRobustly = async (email: string) => {
          const row = page.getByRole('row').filter({ hasText: email });
          const pageIndicator = page.locator('small', { hasText: /Showing page/ });

          while (true) {
              //try the actual page
              try {
                  await expect(row).toBeVisible({ timeout: 2000 });
                  return true; //finded
              } catch (e) {
                  //not this page, we try again
              }

              // next button
              const nextBtn = page.getByRole('button', { name: '»', exact: true });

              const isNextDisabled = await nextBtn.isDisabled() || 
                                     await page.locator('li.page-item.disabled button', { hasText: '»' }).count() > 0;

              if (!await nextBtn.isVisible() || isNextDisabled) {
                  return false; //no more pages
              }

              //next page
              const currentText = await pageIndicator.textContent();
              await nextBtn.click({ force: true });

              //wait till the number of page change
              await expect(pageIndicator).not.toHaveText(currentText!, { timeout: 5000 });
          }
      };

      const found = await findUserRobustly(uniqueUserEmail);
      expect(found, `El usuario ${uniqueUserEmail} no apareció en ninguna página de la tabla`).toBeTruthy();

      const userRow = page.getByRole('row').filter({ hasText: uniqueUserEmail });
      await userRow.getByTitle('See Reservations').click();

      const reservationReason = page.getByText(uniqueReason);
      await expect(reservationReason).toBeVisible();
    });

  });
});