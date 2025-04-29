package com.lmg.crawler_qa_tester.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.logging.Logger;

public class WebDriverFactory {
    private static final Logger LOGGER = Logger.getLogger(WebDriverFactory.class.getName());

    public static WebDriver createDriver(String browserType, boolean headless) {

        WebDriver driver;
        switch (browserType.toLowerCase()) {
            case "chrome":
                LOGGER.info("Creating Chrome WebDriver" + (headless ? " in headless mode" : ""));
                ChromeOptions chromeOptions = new ChromeOptions();
                if (headless) {
                    chromeOptions.addArguments("--headless");
                }
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.addArguments("--window-size=1920,1080");
                chromeOptions.addArguments("--disable-extensions");
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                driver = new ChromeDriver(chromeOptions);
                break;

            case "firefox":
                LOGGER.info("Creating Firefox WebDriver" + (headless ? " in headless mode" : ""));
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) {
                    firefoxOptions.addArguments("--headless");
                }
                driver = new FirefoxDriver(firefoxOptions);
                break;

            default:
                LOGGER.warning(
                    "Unsupported browser type: " + browserType + ". Defaulting to Chrome.");
                driver = new ChromeDriver();
        }

        return driver;
    }

}
