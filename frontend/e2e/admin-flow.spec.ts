import { test, expect } from '@playwright/test';

test.describe('Gestión de Administrador', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.getByPlaceholder('Email Address').fill('admin@studyspace.com');
    await page.locator('input[placeholder="Enter password"]').fill('Admin12.');
    await page.getByRole('main').getByRole('button', { name: 'Log In' }).click();
    
    await expect(page).toHaveURL('/');
    await expect(page.locator('#dropdownProfile')).toBeVisible();
  });

  test('Admin debe poder crear un Software y luego un Aula que lo contenga', async ({ page }) => {
    const softwareName = 'Software E2E ' + Date.now();
    const roomName = 'Aula E2E ' + Date.now();

    // ==========================================
    // CREATE SOFTWARE
    // ==========================================
    console.log(`Creado software: ${softwareName}`);
    await page.getByRole('button', { name: 'Admin Dashboard' }).click();
    await page.getByRole('button', { name: 'Manage Software' }).click();
    await page.getByRole('button', { name: 'Add Software' }).click();

    await page.getByLabel('Name').fill(softwareName);
    await page.getByLabel('Version').fill('1.0');
    await page.getByLabel('Description').fill('Test automático');

    await page.getByRole('button', { name: 'Save' }).click();

    await expect(page).toHaveURL('/admin/softwares');

    const lastPageBtn = page.getByRole('button', { name: '»»' });
    if (await lastPageBtn.isVisible() && await lastPageBtn.isEnabled()) {
        await lastPageBtn.click();
        await page.waitForTimeout(500); 
    }

    const newSoftwareText = page.locator('table').getByText(softwareName);
    await newSoftwareText.scrollIntoViewIfNeeded();
    await expect(newSoftwareText).toBeVisible();


    // ==========================================
    // CREATE ROOM
    // ==========================================
    console.log(`Creado aula: ${roomName}`);
    
    await page.getByRole('button', { name: 'Back to Admin menu' }).click();
    await expect(page).toHaveURL('/admin');

    await page.getByRole('button', { name: 'Manage rooms' }).click();
    await expect(page).toHaveURL('/admin/rooms');
    
    await page.getByRole('button', { name: 'Create New Room' }).click();

    await page.getByLabel('Name').fill(roomName);
    await page.getByLabel('Capacity').fill('25');
    await page.getByLabel('Campus').selectOption('MOSTOLES'); 
    await page.getByLabel('Location').fill('Edificio Tecnológico, Planta 1');

    const selectSoftware = page.getByLabel('Installed Software');
    await selectSoftware.scrollIntoViewIfNeeded();
    const optionElement = selectSoftware.locator('option').filter({ hasText: softwareName });
    const fullOptionLabel = await optionElement.textContent();

    if (fullOptionLabel) {
        await selectSoftware.selectOption({ label: fullOptionLabel.trim() });
    }

    await page.getByRole('button', { name: 'Save' }).click();

    // ==========================================
    // FINAL VALIDATION
    // ==========================================
    await expect(page).toHaveURL('/admin/rooms');
    
    //last page room is where the new one is
    const lastPageRoomsBtn = page.getByRole('button', { name: '»»' });
    if (await lastPageRoomsBtn.isVisible() && await lastPageRoomsBtn.isEnabled()) {
        await lastPageRoomsBtn.click();
        await page.waitForTimeout(500);
    }
    
    await page.getByText(roomName).scrollIntoViewIfNeeded();
    await expect(page.getByText(roomName)).toBeVisible();
  });

});