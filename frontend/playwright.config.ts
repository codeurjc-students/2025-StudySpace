import { defineConfig, devices } from '@playwright/test';
import path from 'path';


export default defineConfig({
  testDir: path.resolve(__dirname, 'e2e'),
  testIgnore: [
    '**/src/**',          
    '**/node_modules/**',
  ],
  fullyParallel: true,
  forbidOnly: !!process.env['CI'],
  retries: process.env['CI'] ? 2 : 0,
  workers: process.env['CI'] ? 1 : undefined,
  reporter: 'html',
  use: {
    //baseURL: 'https://localhost:4200',
    baseURL: 'https://localhost',
    ignoreHTTPSErrors: true,
    trace: 'on-first-retry',
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },

    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },

    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },

   
  ],

  
});
