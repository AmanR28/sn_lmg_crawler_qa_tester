package com.lmg.crawler_qa_tester.config;

import com.lmg.crawler_qa_tester.constants.AppConstant;
import com.lmg.crawler_qa_tester.dto.Domain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Objects;

@Configuration
public class DomainConfig {
    @Bean
    public Domain prodDomain(Environment environment) {

        String name = AppConstant.PROD;
        Boolean browserHeadless = Objects.requireNonNull(
            environment.getProperty("env.prod.browserHeadless", Boolean.class));
        String browserType =
            Objects.requireNonNull(environment.getProperty("env.prod.browserType"));
        Integer consumerThread =
            Objects.requireNonNull(
                environment.getProperty("env.prod.consumerThread", Integer.class));
        Integer pollerRate =
            Objects.requireNonNull(environment.getProperty("env.prod.pollerRate", Integer.class));
        return Domain.builder().name(name).browserHeadless(browserHeadless).browserType(browserType)
            .consumerThread(consumerThread).pollerRate(pollerRate).build();
    }

}
