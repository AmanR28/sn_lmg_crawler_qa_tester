package com.lmg.crawler_qa_tester.util;

import com.microsoft.playwright.Browser;
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

    public static Browser getProdWebDriver(String url) {
        boolean isHeadless = Boolean.parseBoolean(System.getProperty("env.prod.browserHeadless", "true"));
        Playwright playwright = Playwright.create();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(isHeadless)
                .setArgs(getOptions());
        return playwright.chromium().launch(launchOptions);
    }

    public static Browser getPreProdWebDriver(String url) {
        boolean isHeadless = Boolean.parseBoolean(System.getProperty("env.preprod.browserHeadless", "true"));
        Playwright playwright = Playwright.create();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(isHeadless)
                .setArgs(getOptions());
        return playwright.chromium().launch(launchOptions);
    }
}
