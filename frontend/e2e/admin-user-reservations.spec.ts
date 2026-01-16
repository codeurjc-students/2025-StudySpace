import { test, expect } from '@playwright/test';

test.describe('Gestión de Reservas de Usuarios por Admin', () => {

  test('Admin debe poder ver las reservas de un usuario específico', async ({ page }) => {
    
    const uniqueReason = 'Reunión E2E ' + Date.now();
    
    //future date to avoid colisions with other test E2E
    const randomDays = Math.floor(Math.random() * 8) + 2; 
    const targetDate = new Date();
    targetDate.setDate(targetDate.getDate() + randomDays); 
    const dateStr = targetDate.toISOString().split('T')[0];

    // ==========================================
    // THE USER CREATES THE RESERVATION
    // ==========================================
    await test.step('Usuario crea una reserva', async () => {
      await page.goto('/login');
      await page.getByPlaceholder('Email Address').fill('test@test.com');
      await page.locator('input[placeholder="Enter password"]').fill('password123');
      await page.getByRole('main').getByRole('button', { name: 'Log In' }).click();
      await expect(page).toHaveURL('/');

      await page.getByRole('button', { name: 'Book a room' }).click();
      
      const roomSelect = page.locator('select[name="roomId"]');
      await expect(roomSelect).not.toBeDisabled();
      await roomSelect.selectOption({ index: 1 });

      await page.fill('input[name="selectedDate"]', dateStr);
      await page.locator('input[name="selectedDate"]').dispatchEvent('change');

      const startSelect = page.locator('select[name="startTime"]');
      await expect(startSelect).toBeEnabled();
      
      const optionsCount = await startSelect.locator('option').count();
      if (optionsCount > 3) {
          await startSelect.selectOption({ index: 3 }); 
      } else {
          await startSelect.selectOption({ index: 1 });
      }

      const endSelect = page.locator('select[name="endTime"]');
      await expect(endSelect).toBeEnabled();
      await endSelect.selectOption({ index: 1 });

      await page.locator('textarea[name="reason"]').fill(uniqueReason);

      await page.getByRole('button', { name: 'Confirm Reservation' }).click();
      
      await expect(page).toHaveURL('/');
    });

    // ==========================================
    // LOGOUT
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
    // ADMIN CONSULTATION
    // ==========================================
    await test.step('Admin busca al usuario y ve sus reservas', async () => {
      await page.getByPlaceholder('Email Address').fill('admin@studyspace.com');
      await page.locator('input[placeholder="Enter password"]').fill('password');
      await page.getByRole('main').getByRole('button', { name: 'Log In' }).click();
      
      await page.getByRole('button', { name: 'Admin Dashboard' }).click();
      await page.getByRole('button', { name: 'Manage Users' }).click();
      
      //search the user
      const userRow = page.getByRole('row').filter({ hasText: 'test@test.com' });
      await expect(userRow).toBeVisible();
      
      await userRow.getByTitle('See Reservations').click();

      //search the booking
      //as it is desc order the new one alwais first
      const reservationReason = page.getByText(uniqueReason);
      await expect(reservationReason).toBeVisible();
    });

  });
});