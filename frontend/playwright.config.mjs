import { defineConfig } from "@playwright/test";
import process from "node:process";

export default defineConfig({
  testDir: "./tests",
  timeout: 90000,
  expect: { timeout: 15000 },
  workers: 1,
  retries: 0,
  outputDir: ".build/browser-results",
  reporter: [["list"], ["json", { outputFile: ".build/browser-results.json" }]],
  use: {
    baseURL: "http://127.0.0.1:4174",
    channel: process.env.PLAYWRIGHT_CHANNEL || "chrome",
    headless: true,
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
  },
  webServer: [
    {
      command: `"${process.env.JAVA_EXE || "java"}" -jar ../Backend/target/Backend-0.0.1-SNAPSHOT.jar --server.port=18082 --spring.jpa.show-sql=false --debug=false`,
      url: "http://127.0.0.1:18082/api/jobs",
      env: { DB_URL: process.env.E2E_DB_URL || "jdbc:mysql://localhost:3306/skill_gap_analyzer_browser_tests?createDatabaseIfNotExist=true", CORS_ALLOWED_ORIGINS: "http://127.0.0.1:4174" },
      timeout: 90000,
      reuseExistingServer: false,
    },
    {
      command: "npm run preview -- --port 4174",
      url: "http://127.0.0.1:4174",
      env: { API_PROXY_TARGET: "http://127.0.0.1:18082" },
      timeout: 30000,
      reuseExistingServer: false,
    },
  ],
});
