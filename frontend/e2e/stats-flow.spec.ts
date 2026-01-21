import { test, expect } from '@playwright/test';

test.describe('Flujo de Estadísticas y Reservas', () => {

  test('Un Usuario debe poder reservar y un Admin ver reflejada la estadística', async ({ page }) => {
    
    //to avoid colisions date to the future many days
    const randomDays = Math.floor(Math.random() * 8) + 2; 
    const targetDate = new Date();
    targetDate.setDate(targetDate.getDate() + randomDays); 
    const dateStr = targetDate.toISOString().split('T')[0];

    // ==========================================
    // USER BOOKS A ROOM
    // ==========================================
    await test.step('Usuario crea una reserva', async () => {
      await page.goto('/login');
      await page.getByPlaceholder('Email Address').fill('carlos@urjc.es');
      await page.locator('input[placeholder="Enter password"]').fill('1234aA..');
      await page.getByRole('main').getByRole('button', { name: 'Log In' }).click();
      await expect(page).toHaveURL('/');

      await page.getByRole('button', { name: 'Book a room' }).click();
      await expect(page).toHaveURL('/reservations/create');

      const roomSelect = page.locator('select[name="roomId"]');
      await expect(roomSelect).not.toBeDisabled();
      await roomSelect.selectOption({ index: 1 }); 

      await page.fill('input[name="selectedDate"]', dateStr);
      await page.locator('input[name="selectedDate"]').dispatchEvent('change');

      const startSelect = page.locator('select[name="startTime"]');
      await expect(startSelect).toBeEnabled();
      
        // to not colide
      const optionsCount = await startSelect.locator('option').count();
      if (optionsCount > 3) {
          await startSelect.selectOption({ index: 3 }); 
      } else {
          await startSelect.selectOption({ index: 1 });
      }

      const endSelect = page.locator('select[name="endTime"]');
      await expect(endSelect).toBeEnabled();
      await endSelect.selectOption({ index: 1 }); // 30 min de duración

      await page.locator('textarea[name="reason"]').fill('Reserva E2E para Estadísticas');
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
      await expect(page.getByPlaceholder('Email Address')).toBeVisible();
    });

    // ==========================================
    // ADMIN VERIFIES STATISTICS
    // ==========================================
    await test.step('Admin verifica las estadísticas', async () => {
      await page.getByPlaceholder('Email Address').fill('admin@studyspace.com');
      await page.locator('input[placeholder="Enter password"]').fill('Admin12.');
      await page.getByRole('main').getByRole('button', { name: 'Log In' }).click();
      await expect(page).toHaveURL('/');

      await page.getByRole('button', { name: 'Admin Dashboard' }).click();
      await expect(page).toHaveURL('/admin');
      
      await page.getByRole('button', { name: 'Occupancy Statistics' }).click();
      await expect(page).toHaveURL('/admin/stats');
        //filter
      await page.fill('#statsDate', dateStr);
      await page.locator('#statsDate').dispatchEvent('change');
        //verify graph
      await page.waitForTimeout(1000); 
      await expect(page.locator('canvas').first()).toBeVisible();
      expect(await page.locator('canvas').count()).toBeGreaterThanOrEqual(1);
    });

  });
});