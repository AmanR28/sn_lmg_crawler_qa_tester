package com.lmg.crawler_qa_tester.config;

import com.lmg.crawler_qa_tester.constants.PreProdDomainConstant;
import com.lmg.crawler_qa_tester.constants.ProdDomainConstant;
import com.lmg.crawler_qa_tester.model.Domain;
import com.lmg.crawler_qa_tester.model.Report;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public Domain prodDomain() {
       return Domain.builder().name(ProdDomainConstant.NAME).build();
    };

    @Bean
    public Domain preProdDomain() {
        return Domain.builder().name(PreProdDomainConstant.NAME).build();
    };

    @Bean
    public Report report() {
        return new Report();
    }
}
