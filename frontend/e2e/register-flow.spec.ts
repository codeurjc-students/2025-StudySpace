import { test, expect } from '@playwright/test';

test.describe('Flujo de Registro y Perfil de Usuario', () => {

  test('Un usuario nuevo debe poder registrarse, iniciar sesión y ver su perfil', async ({ page }) => {
    
    const timestamp = Date.now();
    const newUser = {
      name: `Usuario Test ${timestamp}`,
      email: `newuser_${timestamp}@test.com`,
      password: 'TestPass123!' 
    };

    // ==========================================
    // REGISTER
    // ==========================================
    await test.step('Registro de nuevo usuario', async () => {
      await page.goto('/login');
      await page.getByRole('button', { name: 'Register' }).click();
      
      await page.getByLabel('Name').fill(newUser.name);
      await page.getByLabel('Email').fill(newUser.email);
      await page.locator('input[placeholder="Create a password"]').fill(newUser.password);

      const dialogPromise = page.waitForEvent('dialog');
      await page.getByRole('button', { name: 'Sign Up' }).click();
      const dialog = await dialogPromise;
      await dialog.dismiss(); 

      await expect(page).toHaveURL('/login');
    });

    // ==========================================
    //LOGIN
    // ==========================================
    await test.step('Login con el nuevo usuario', async () => {
      await page.getByPlaceholder('Email Address').fill(newUser.email);
      await page.locator('input[placeholder="Enter password"]').fill(newUser.password);
      await page.getByRole('main').getByRole('button', { name: 'Log In' }).click();

      await expect(page).toHaveURL('/');
      await expect(page.locator('#dropdownProfile')).toBeVisible();
    });

    // ==========================================
    // VERIFY PROFILE 
    // ==========================================
    await test.step('Acceder al perfil y verificar datos', async () => {
      await page.locator('#dropdownProfile').click();
      
      await page.getByRole('link', { name: 'View My Profile' }).click();

      await expect(page).toHaveURL(/\/profile|\/users\/\d+/);

      const mainContent = page.locator('main');
      
      // name as title or in main
      await expect(mainContent.getByText(newUser.name)).toBeVisible();
      await expect(mainContent.getByText(newUser.email)).toBeVisible();
    });

    // ==========================================
    // LOGOUT
    // ==========================================
    await test.step('Logout', async () => {
       if (!await page.getByRole('button', { name: 'Log Out' }).isVisible()) {
          await page.locator('#dropdownProfile').click();
       }
       await page.getByRole('button', { name: 'Log Out' }).click();
       await expect(page.locator('#dropdownProfile')).not.toBeVisible();
    });

  });
});