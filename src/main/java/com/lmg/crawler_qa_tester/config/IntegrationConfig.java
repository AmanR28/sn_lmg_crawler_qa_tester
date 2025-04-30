package com.lmg.crawler_qa_tester.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessageSource;

@Configuration
public class IntegrationConfig {
    @Bean
    public PublishSubscribeChannel prodChannel() {

        return null;
    }

    @Bean
    public PublishSubscribeChannel preProdChannel() {

        return null;
    }

    @Bean
    @InboundChannelAdapter()
    public MessageSource<?> prodMessagePoller() {

        return null;
    }

    @Bean
    @InboundChannelAdapter()
    public MessageSource<?> preProdMessagePoller() {

        return null;
    }

}
