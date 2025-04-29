package com.lmg.crawler_qa_tester.config;

import com.lmg.crawler_qa_tester.model.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class IntegrationConfig {
    @Autowired
    private Domain prodDomain;
    @Autowired
    private Domain preProdDomain;

    private PublishSubscribeChannel createChannel(Domain domain) {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(domain.getConsumersThread());
        executor.setMaxPoolSize(domain.getConsumersThread());
        executor.setThreadNamePrefix("TP_" + domain.getName());
        executor.initialize();
        return new PublishSubscribeChannel(executor);
    }

    @Bean
    public PublishSubscribeChannel prodChannel() {

        return createChannel(prodDomain);
    }

    @Bean
    public PublishSubscribeChannel preProdChannel() {

        return createChannel(preProdDomain);
    }

}
