package com.lmg.crawler_qa_tester.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import com.lmg.crawler_qa_tester.model.Domain;
import com.lmg.crawler_qa_tester.util.WebDriverFactory;

import java.util.concurrent.TimeUnit;

import static com.lmg.crawler_qa_tester.util.Constants.DEFAULT_CONFIG_PAGE_LOADOUT_TIME;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DriverConfig {
    @Qualifier("prodDomain")
    private final Domain prodDomain;
    @Qualifier("preProdDomain")
    private final Domain preProdDomain;

    private WebDriver createDriver(Domain domain) {
        log.info("Creating WebDriver for domain: {}", domain.getName());

        WebDriver driver =
            WebDriverFactory.createDriver(domain.getBrowserType(), domain.isBrowserHeadless());
        driver.manage().timeouts()
            .pageLoadTimeout(DEFAULT_CONFIG_PAGE_LOADOUT_TIME, TimeUnit.SECONDS);
        driver.manage().window().maximize();

        driver.get(domain.getBaseUrl());
        driver.manage().addCookie(new Cookie("preprod", "true", null, "/", null));

        return driver;
    }

    @Bean
    @Scope("prototype")
    public WebDriver prodDriver() {
        return createDriver(prodDomain);
    }

    @Bean
    @Scope("prototype")
    public WebDriver preProdDriver() {
        return createDriver(preProdDomain);
    }

}
