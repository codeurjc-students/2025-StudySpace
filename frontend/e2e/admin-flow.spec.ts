import { test, expect } from '@playwright/test';

test.describe('Administrator Management', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.getByPlaceholder('Email Address').fill('admin@studyspace.com');
    await page.locator('input[placeholder="Enter password"]').fill('Admin12.');
    await page.getByRole('main').getByRole('button', { name: 'Log In' }).click();
    
    await expect(page).toHaveURL('/');
    await expect(page.locator('#dropdownProfile')).toBeVisible();
  });

  test('The administrator should be able to create software and then a classroom that contains it.', async ({ page }) => {
    const timestamp = Date.now();
    const softwareName = 'Soft ' + timestamp; 
    const roomName = 'Aula ' + timestamp;

    // --- search function for pagination ---
    const findRowInTable = async (searchText: string) => {
        const row = page.locator('tr').filter({ hasText: searchText });
        
        //on actual page?
        if (await row.isVisible()) return true;

        //locator the pagitanion
        const pageIndicator = page.locator('small', { hasText: /Showing page/ });
        
        //no pagination adn not found
        if (!await pageIndicator.isVisible()) return false;

        //to search trought pages
        while (true) {
            const nextBtn = page.getByRole('button', { name: '»', exact: true });

            //no more next button --> stop
            if (!await nextBtn.isVisible() || await nextBtn.isDisabled()) {
                return false;
            }

            //actual text
            const currentText = await pageIndicator.textContent();

            //next
            await nextBtn.click({ force: true });

            // wait till text change
            await expect(pageIndicator).not.toHaveText(currentText!, { timeout: 10000 });

            if (await row.isVisible()) return true;
        }
    };

    // ==========================================
    // CREATE SOFTWARE
    // ==========================================
    await page.getByRole('button', { name: 'Admin Dashboard' }).click();
    await page.getByRole('button', { name: 'Manage Software' }).click();
    await page.getByRole('button', { name: 'Add Software' }).click();

    await page.getByLabel('Name').fill(softwareName);
    await page.getByLabel('Version').fill('1.0');
    await page.getByLabel('Description').fill('Auto Test');
    await page.getByRole('button', { name: 'Save' }).click();

    await expect(page).toHaveURL('/admin/softwares');

    // VErify software
    const softwareFound = await findRowInTable(softwareName);
    expect(softwareFound, `El software ${softwareName} no se encontró`).toBeTruthy();
    await expect(page.locator('tr').filter({ hasText: softwareName })).toBeVisible();


    // ==========================================
    // CREATE ROOM
    // ==========================================
    await page.getByRole('button', { name: 'Back to Admin menu' }).click();
    await page.getByRole('button', { name: 'Manage rooms' }).click();
    await page.getByRole('button', { name: 'Create New Room' }).click();

    await page.getByLabel('Name').fill(roomName);
    await page.getByLabel('Capacity').fill('25');
    await page.getByLabel('Campus').selectOption({ value: 'MOSTOLES' }); 
    await page.getByLabel('Location').fill('Edificio Test');

    const selectSoftware = page.getByLabel('Installed Software');
    const targetOption = selectSoftware.locator('option').filter({ hasText: softwareName });
    await expect(targetOption).toHaveCount(1);
    const optionText = await targetOption.textContent();
    if (optionText) {
        await selectSoftware.selectOption({ label: optionText.trim() }); 
    }

    await page.getByRole('button', { name: 'Save' }).click();

    // ==========================================
    // FINAL VALIDATION (ROOM)
    // ==========================================
    await expect(page).toHaveURL('/admin/rooms');
    
    //pagination search
    const roomFound = await findRowInTable(roomName);
    expect(roomFound, `El aula ${roomName} no se encontró`).toBeTruthy();
    
    await expect(page.locator('tr').filter({ hasText: roomName })).toBeVisible();
  });

});