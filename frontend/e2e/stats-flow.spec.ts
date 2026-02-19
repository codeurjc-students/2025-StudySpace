import { test, expect } from '@playwright/test';

test.describe('Statistics and Reservations Flow', () => {

  test('A user creates a reservation and the admin views the statistics.', async ({ page,request }) => {
    
    //data for test
    const timestamp = Date.now();
    const userEmail = `stats_user_${timestamp}@e2e.com`;
    const userPass = 'StatsPass123.';
    const userName = `Stats User ${timestamp}`;

    // avoid weekends
    const targetDate = new Date();
    targetDate.setDate(targetDate.getDate() + 5); 
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

      await page.getByRole('button', { name: /Book a room/i }).click();
        


        const roomSelect = page.locator('select[name="roomId"]');
      await expect(roomSelect).not.toBeDisabled();
      await roomSelect.selectOption({ index: 2 }); 
      await page.waitForTimeout(1000); 

      const dateInput = page.getByLabel('2. Select Date');
      await dateInput.evaluate((el: HTMLInputElement, dateValue) => {
          el.value = dateValue;
          el.dispatchEvent(new Event('input', { bubbles: true }));
          el.dispatchEvent(new Event('change', { bubbles: true }));
          el.dispatchEvent(new FocusEvent('blur', { bubbles: true }));
      }, dateStr);
      
      await page.waitForTimeout(3000);


        const startSelect = page.locator('select[name="startTime"]');
        await expect(startSelect).toBeEnabled({ timeout: 15000 }); 
        await expect(startSelect.locator('option').nth(1)).toBeAttached({ timeout: 10000 });
        await startSelect.selectOption({ index: 1 });

        await page.waitForTimeout(500);

        const endSelect = page.locator('select[name="endTime"]');
        await expect(endSelect).toBeEnabled({ timeout: 15000 });
        await expect(endSelect.locator('option').nth(1)).toBeAttached({ timeout: 10000 });
        await endSelect.selectOption({ index: 1 });

        await page.locator('textarea[name="reason"]').fill(`Stats Check ${timestamp}`);   

        await page.getByRole('button', { name: 'Confirm Reservation' }).click();






      let message = null;
      for (let i = 0; i < 20; i++) {
          try {
              const response = await request.get('http://127.0.0.1:8025/api/v2/messages');
              if (response.ok()) {
                  const emailData = await response.json();
                  const found = emailData.items.find((msg: any) => 
                      msg.Content.Headers.To && msg.Content.Headers.To[0].includes(userEmail)
                  );
                  if (found) {
                      message = found;
                      break; 
                  }
              }
          } catch (e) { }
          await page.waitForTimeout(1000); 
      }
      
      expect(message, 'The verification email was not found in MailHog').toBeTruthy();

      const cleanBody = message.Content.Body.replace(/=\r?\n/g, '');
      const match = cleanBody.match(/token=([a-zA-Z0-9-]+)/);
      
      if (match) {
          const token = match[1]; 
          
          await page.goto(`/verify-reservation?token=${token}`);
          
          await expect(page.getByText(/confirmed successfully|Reservation Confirmed/i)).toBeVisible();
      } else {
          throw new Error(`HTTPS link not found in the email body. Text received: ${cleanBody}`);
      }
      
      await page.goto('/');
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