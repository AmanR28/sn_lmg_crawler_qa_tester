package com.lmg.crawler_qa_tester.util;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

public class BrowserFactory {
    public static List<String> getOptions() {
        return Arrays.asList(new String[]{
                "--disable-blink-features=AutomationControlled",
                "--no-sandbox",
                "--disable-dev-shm-usage"
        });
    }

    public static Browser getProdWebDriver() {
        boolean isHeadless = Boolean.parseBoolean(System.getProperty("env.prod.browserHeadless", "true"));
        Playwright playwright = Playwright.create();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(isHeadless)
                .setArgs(getOptions());
        return playwright.chromium().launch(launchOptions);
    }

    public static Browser getPreProdWebDriver() {
        boolean isHeadless = Boolean.parseBoolean(System.getProperty("env.preprod.browserHeadless", "true"));
        Playwright playwright = Playwright.create();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(isHeadless)
                .setArgs(getOptions());
        return playwright.chromium().launch(launchOptions);
    }

    public static BrowserContext getBrowserContext(Browser browser) {
        return browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .setViewportSize(1920, 1080)
        );
    }
}
