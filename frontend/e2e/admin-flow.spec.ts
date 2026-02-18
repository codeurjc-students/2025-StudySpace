import { test, expect } from '@playwright/test';

test.describe('Administrator Management', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    
    // wait till login form is ready
    const emailInput = page.getByPlaceholder('Email Address');
    await expect(emailInput).toBeEditable();
    
    await emailInput.fill('admin@studyspace.com');
    await page.locator('input[placeholder="Enter password"]').fill('Admin12.');
    
    await Promise.all([
      page.waitForURL('/', { timeout: 15000 }), 
      page.getByRole('main').getByRole('button', { name: 'Log In' }).click()
    ]);
    
    await expect(page.locator('#dropdownProfile')).toBeVisible();
  });

  test('The administrator should be able to create software and then a classroom that contains it.', async ({ page }) => {
    const timestamp = Date.now();
    const softwareName = 'Soft ' + timestamp; 
    const roomName = 'Aula ' + timestamp;

    // --- search function for pagination ---
    const findRowInTable = async (searchText: string) => {
        await expect(page.locator('tbody tr').first()).toBeVisible({ timeout: 10000 });
        const row = page.locator('tr').filter({ hasText: searchText });
        const pageIndicator = page.locator('small', { hasText: /Showing page/ });

        while (true) {
            //actual page
            try {
                await expect(row).toBeVisible({ timeout: 2000 });//2 seconds
                return true; //found
            } catch (e) {
                // not this page, maybe next
            }

            //next button
            const nextBtn = page.getByRole('button', { name: '»', exact: true });

            //button exists
            const isParentDisabled = await page.locator('li.page-item.disabled').filter({ has: nextBtn }).count() > 0;
            
            if (!await nextBtn.isVisible() || isParentDisabled) {
                return false; //no more pages not found
            }

            //next page
            const currentText = await pageIndicator.textContent();
            await nextBtn.click({ force: true });
            //wait for it
            await expect(pageIndicator).not.toHaveText(currentText!, { timeout: 10000 });
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
    expect(softwareFound, `The software ${softwareName} was not found`).toBeTruthy();
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

    // ==========================================
    // CLEANUP 
    // ==========================================
    await test.step('Cleanup: Delete the room and the software created', async () => {
        
        const roomRow = page.locator('tr').filter({ hasText: roomName });
        await expect(roomRow).toBeVisible();

        page.on('dialog', dialog => dialog.accept());
        await roomRow.getByRole('button', { name: /🗑️|Delete/ }).click();
        
        // Wait for the room to truly disappear
        await expect(page.locator('tr').filter({ hasText: roomName })).not.toBeVisible();

        //deleete software
        await page.getByRole('button', { name: 'Back to Admin menu' }).click(); 
        
        await page.getByRole('button', { name: 'Manage Software' }).click();
        await expect(page).toHaveURL('/admin/softwares');

        const softRow = page.locator('tr').filter({ hasText: softwareName });
        
        if (await softRow.isVisible()) {
            await softRow.getByRole('button', { name: /🗑️|Delete/ }).click();
            await expect(softRow).not.toBeVisible();
        }
    });
  });

});