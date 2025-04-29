package com.lmg.crawler_qa_tester.config;

import com.lmg.crawler_qa_tester.model.Domain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Objects;

import static com.lmg.crawler_qa_tester.util.Constants.*;

@Configuration
public class DomainConfig {


    @Bean
    public Domain prodDomain(Environment environment) {
        String name = PROD_ENVIRONMENT;
        String baseUrl = Objects.requireNonNull(environment.getProperty("env.prod.base-url"));
        boolean browserHeadless = environment.getProperty("env.prod.web-driver.headless", Boolean.class, DEFAULT_CONFIG_BROWSER_HEADLESS);
        String browserType = environment.getProperty("env.prod.web-driver.browser", DEFAULT_CONFIG_BROWSER_TYPE);
        int consumerThread = environment.getProperty("env.prod.consumer_thread", Integer.class, DEFAULT_CONFIG_CONSUMER_THREADS);

        Domain domain = Domain.builder().name(name).baseUrl(baseUrl).browserHeadless(browserHeadless).browserType(browserType).consumersThread(consumerThread).build();
        return domain;
    }

    @Bean
    public Domain preProdDomain(Environment environment) {
        String name = PRE_PROD_ENVIRONMENT;
        String baseUrl = Objects.requireNonNull(environment.getProperty("env.pre_prod.base-url"));
        boolean browserHeadless = environment.getProperty("env.pre_prod.web-driver.headless", Boolean.class, DEFAULT_CONFIG_BROWSER_HEADLESS);
        String browserType = environment.getProperty("env.pre_prod.web-driver.browser", DEFAULT_CONFIG_BROWSER_TYPE);
        int consumerThread = environment.getProperty("env.pre_prod.consumer_thread", Integer.class, DEFAULT_CONFIG_CONSUMER_THREADS);

        Domain domain = Domain.builder().name(name).baseUrl(baseUrl).browserHeadless(browserHeadless).browserType(browserType).consumersThread(consumerThread).build();
        return domain;
    }

}
