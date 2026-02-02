import { test, expect } from '@playwright/test';

test.describe('Autenticación de Usuarios', () => {

  test('Debe permitir login con credenciales correctas y mostrar el perfil', async ({ page }) => {
    await page.goto('/login');

    await page.getByPlaceholder('Email Address').fill('fran@gmail.com');
    await page.locator('input[placeholder="Enter password"]').fill('1234aA..');


    await page.getByRole('main').getByRole('button', { name: 'Log In', exact: true }).click();

    //verify
    await expect(page).toHaveURL('/');
    await expect(page.getByRole('button', { name: 'Log In' }).first()).not.toBeVisible();

    const profileDropdown = page.locator('#dropdownProfile');
    await expect(profileDropdown).toBeVisible();
    //profile button have text
    await expect(profileDropdown).toContainText('Francisco Blanco');
  });

  test('Debe mostrar error con credenciales incorrectas', async ({ page }) => {
    await page.goto('/login');

    await page.getByPlaceholder('Email Address').fill('fake@test.com');
    await page.locator('input[placeholder="Enter password"]').fill('wrongpass');
    
    await page.getByRole('main').getByRole('button', { name: 'Log In', exact: true }).click();
    //verify
    await expect(page.getByText('Incorrect username or password')).toBeVisible();
    await expect(page).toHaveURL('/login');
  });

});