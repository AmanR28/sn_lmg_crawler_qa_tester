package com.lmg.crawler_qa_tester.service;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import com.lmg.crawler_qa_tester.model.Domain;
import com.lmg.crawler_qa_tester.util.WebDriverFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.lmg.crawler_qa_tester.util.Constants.DEFAULT_CONFIG_PAGE_LOADOUT_TIME;

@Service
@Slf4j
public class DriverService {
    @Autowired
    private Domain prodDomain;
    @Autowired
    private Domain preProdDomain;

    private WebDriver createDriver(Domain domain) {

        log.info("Creating WebDriver for domain: {}", domain.getName());

        WebDriver driver =
            WebDriverFactory.createDriver(domain.getBrowserType(), domain.isBrowserHeadless());
        driver.manage().timeouts()
            .pageLoadTimeout(DEFAULT_CONFIG_PAGE_LOADOUT_TIME, TimeUnit.SECONDS);
        driver.manage().window().maximize();

        if ("PRE_PROD".equals(domain.getName())) {
            driver.get(domain.getBaseUrl());
            driver.manage().addCookie(new Cookie("preprod", "true", null, "/", null));
        }

        return driver;
    }

    public WebDriver getProdDriver() {

        return createDriver(prodDomain);
    }

    public WebDriver getPreProdDriver() {

        return createDriver(preProdDomain);
    }

}
