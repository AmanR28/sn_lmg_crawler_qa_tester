package com.lmg.crawler_qa_tester.config;

import com.lmg.crawler_qa_tester.constants.AppConstant;
import com.lmg.crawler_qa_tester.dto.Domain;
import com.lmg.crawler_qa_tester.dto.Report;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public Domain prodDomain() {
       return Domain.builder().name(AppConstant.PROD).build();
    };

    @Bean
    public Domain preProdDomain() {
        return Domain.builder().name(AppConstant.PRE_PROD).build();
    };

    @Bean
    public Report report() {
        return new Report();
    }
}
