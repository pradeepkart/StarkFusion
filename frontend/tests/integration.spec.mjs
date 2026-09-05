import { test, expect } from "@playwright/test";
import { randomUUID } from "node:crypto";
import process from "node:process";

async function signIn(page, email, password) {
  await page.getByLabel("Email", { exact: true }).fill(email);
  await page.getByLabel("Password", { exact: true }).fill(password);
  await page.getByRole("button", { name: "Sign in", exact: true }).click();
  await expect(page.getByRole("button", { name: "Sign out", exact: true })).toBeVisible();
}
async function nav(page, name) {
  await page.getByRole("navigation").getByRole("button", { name, exact: true }).click();
}

test("admin publishes requirements, student registers and applies, admin selects, state persists", async ({ page }, testInfo) => {
  const errors = [];
  page.on("pageerror", (error) => errors.push(error.message));
  const suffix = randomUUID().slice(0, 8);
  const java = `Java ${suffix}`;
  const sql = `SQL ${suffix}`;
  const title = `Developer ${suffix}`;
  const student = `Student ${suffix}`;
  const email = `browser-${suffix}@example.com`;
  await page.goto("/");
  await signIn(page, process.env.ADMIN_EMAIL || "admin@skillgap.com", process.env.ADMIN_PASSWORD || "admin123");
  await expect(page.getByRole("heading", { name: "Admin dashboard" })).toBeVisible();
  await nav(page, "skills");
  for (const name of [java, sql]) {
    await page.getByLabel("Skill name", { exact: true }).fill(name);
    await page.getByLabel("Category", { exact: true }).fill("Technical");
    await page.getByRole("button", { name: "Create skill", exact: true }).click();
    await expect(page.getByRole("heading", { name, exact: true })).toBeVisible();
  }
  await nav(page, "jobs");
  await page.getByLabel("Job title", { exact: true }).fill(title);
  await page.getByLabel("Company", { exact: true }).fill("Browser Test Company");
  await page.getByLabel("Location", { exact: true }).fill("Chennai");
  await page.getByRole("button", { name: "Publish job", exact: true }).click();
  await page.getByRole("button", { name: `Manage ${title}`, exact: true }).click();
  await page.getByRole("combobox", { name: "Skill", exact: true }).selectOption({ label: java });
  await page.getByRole("combobox", { name: "Required level", exact: true }).selectOption("4");
  await page.getByLabel("Mandatory", { exact: true }).check();
  await page.getByRole("button", { name: "Save requirement", exact: true }).click();
  await expect(page.getByText(`${java} · level 4 · Mandatory`, { exact: true })).toBeVisible();
  await page.getByRole("combobox", { name: "Skill", exact: true }).selectOption({ label: sql });
  await page.getByRole("combobox", { name: "Required level", exact: true }).selectOption("3");
  await page.getByLabel("Mandatory", { exact: true }).uncheck();
  await page.getByRole("button", { name: "Save requirement", exact: true }).click();
  await expect(page.getByText(`${sql} · level 3 · Optional`, { exact: true })).toBeVisible();

  await page.getByRole("button", { name: "Sign out" }).click();
  await page.getByRole("button", { name: "Sign up", exact: true }).click();
  await page.getByLabel("Full name", { exact: true }).fill(student);
  await page.getByLabel("Email", { exact: true }).fill(email);
  await page.getByLabel("Password", { exact: true }).fill("123456");
  await page.getByRole("button", { name: "Create account", exact: true }).click();
  await expect(page.getByRole("heading", { name: `Welcome, ${student}.`, exact: true })).toBeVisible();
  await nav(page, "skills");
  for (const [skill, level] of [[java, "2"], [sql, "3"]]) {
    await page.getByRole("combobox", { name: "Skill", exact: true }).selectOption({ label: skill });
    await page.getByRole("combobox", { name: "Proficiency", exact: true }).selectOption(level);
    await page.getByRole("button", { name: "Save skill", exact: true }).click();
    await expect(page.getByRole("heading", { name: skill, exact: true })).toBeVisible();
  }
  await nav(page, "jobs");
  const card = page.locator("article").filter({ has: page.getByRole("heading", { name: title, exact: true }) });
  await expect(card.getByText("66.67% match", { exact: true })).toBeVisible();
  await card.getByRole("button", { name: `Apply for ${title}`, exact: true }).click();
  await expect(card.getByRole("button", { name: "Applied", exact: true })).toBeDisabled();
  await nav(page, "Skill gap");
  const gap = page.locator("section").filter({ has: page.getByRole("heading", { name: title, exact: true }) });
  await expect(gap.getByText("Priority 1", { exact: true })).toBeVisible();
  await expect(gap.getByText(`${java} is mandatory. Current level is 2 and required level is 4.`, { exact: false })).toBeVisible();
  await page.screenshot({ path: testInfo.outputPath("student-skill-gap.png"), fullPage: true });
  await page.reload();
  await expect(page.getByRole("heading", { name: `Welcome, ${student}.`, exact: true })).toBeVisible();
  await nav(page, "applications");
  await expect(page.getByRole("row").filter({ hasText: title })).toContainText("APPLIED");
  await page.getByRole("button", { name: "Sign out" }).click();

  await signIn(page, process.env.ADMIN_EMAIL || "admin@skillgap.com", process.env.ADMIN_PASSWORD || "admin123");
  await nav(page, "applications");
  const status = page.getByLabel(`Status for ${student}, ${title}`, { exact: true });
  await status.selectOption("SELECTED");
  await expect(page.getByRole("status")).toHaveText("Application status updated.");
  await expect(status).toHaveValue("SELECTED");
  await nav(page, "students");
  await page.getByRole("button", { name: `View skills for ${student}`, exact: true }).click();
  await expect(page.getByText(`${java}: 2 / 5`, { exact: true })).toBeVisible();
  await page.getByRole("button", { name: "Sign out" }).click();
  await signIn(page, email, "123456");
  await nav(page, "applications");
  await expect(page.getByRole("row").filter({ hasText: title })).toContainText("SELECTED");
  await expect(page.getByRole("row").filter({ hasText: title })).toContainText("66.67%");
  await page.screenshot({ path: testInfo.outputPath("student-applications.png"), fullPage: true });
  expect(errors).toEqual([]);
});

test("bad credentials show server errors and corrupted local session does not crash", async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem("starkfusion-user", "not-json"));
  await page.goto("/");
  await page.getByLabel("Email", { exact: true }).fill("nobody@example.com");
  await page.getByLabel("Password", { exact: true }).fill("bad-password");
  await page.getByRole("button", { name: "Sign in", exact: true }).click();
  await expect(page.getByRole("alert")).toContainText("Invalid email or password");
  await expect(page.getByRole("navigation")).toHaveCount(0);
});

test("a fresh student sees only their profile and applications; invalid token ends the session", async ({ page, request }) => {
  const email = `isolation-${randomUUID()}@example.com`;
  const registered = await request.post("/api/auth/register", { data: { name: "Isolated Student", email, password: "123456" } });
  expect(registered.status()).toBe(201);
  await page.goto("/");
  await signIn(page, email, "123456");
  await nav(page, "applications");
  await expect(page.getByText("No applications yet.", { exact: true })).toBeVisible();
  await nav(page, "profile");
  await expect(page.locator("main").getByText(email, { exact: true })).toBeVisible();
  const denied = await page.evaluate(async () => {
    const response = await fetch("/api/admin/dashboard", { headers: { Authorization: `Bearer ${localStorage.getItem("starkfusion-access-token")}` } });
    return response.status;
  });
  expect(denied).toBe(403);
  await page.evaluate(() => localStorage.setItem("starkfusion-access-token", "invalid-token"));
  await page.getByRole("button", { name: "Refresh", exact: true }).click();
  await expect(page.getByRole("button", { name: "Sign in", exact: true })).toBeVisible();
  expect(await page.evaluate(() => localStorage.getItem("starkfusion-access-token"))).toBeNull();
});
