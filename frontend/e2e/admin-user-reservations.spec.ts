import { test, expect } from '@playwright/test';

test.describe('User Reservation Management by Admin', () => {

  test.setTimeout(120000);

  test('Admin should be able to see a specific users bookings', async ({ page,request }) => {
    
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

      await page.getByRole('button', { name: /Book a room/i }).click();
      
      //room
      await page.locator('select[name="roomId"]').selectOption({ index: 1 });
      const dateInput = page.getByLabel('2. Select Date');
      await dateInput.click();
      await dateInput.fill(dateStr);
      await dateInput.press('Enter'); 
      await dateInput.blur();         
      

      await page.waitForTimeout(2000);

      const startSelect = page.locator('select[name="startTime"]');
      await expect(startSelect).toBeEnabled({ timeout: 10000 }); 
      await startSelect.selectOption({ index: 1 });

      await page.locator('select[name="endTime"]').selectOption({ index: 1 });
      await page.locator('textarea[name="reason"]').fill(uniqueReason);
      
      await page.getByRole('button', { name: 'Confirm Reservation' }).click();
      
      // ---------------------------------------------------------
      // tries for MAILHOG
      // -------------------------------------------------------
        //wait for email
      let message = null;
      for (let i = 0; i < 15; i++) {
          try {
            // 127.0.0.1 for local connection
            const response = await request.get('http://127.0.0.1:8025/api/v2/messages');
            if (response.ok()) {
                const emailData = await response.json();
                
                //search for email
                const found = emailData.items.find((msg: any) => 
                    msg.Content.Headers.To && msg.Content.Headers.To[0].includes(uniqueUserEmail)
                );
                
                if (found) {
                    message = found;
                    console.log("Email found during attempt " + (i+1));
                    break; //end loop
                }
            }
          } catch (e) {
              console.log("Waiting to connect to MailHog...");
          }
          await page.waitForTimeout(1000);
      }
      expect(message, 'The verification email was not found in MailHog after 15 seconds').toBeTruthy();

      const body = message.Content.Body;
        const match = body.match(/https:\/\/[\w.:]+\/verify-reservation\?token=([a-zA-Z0-9-]+)/);
      
      if (match) {
          const link = match[0];
          console.log("Link found:", link); // Debug
          
          await page.goto(link);
          await expect(page.getByText(/confirmed successfully|Reservation Confirmed/i)).toBeVisible();
      } else {
          throw new Error("The link does not match the regular expression. Check the console.log file in the email body.");
      }
      
      await page.goto('/');
        
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