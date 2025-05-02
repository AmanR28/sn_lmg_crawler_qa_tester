package com.lmg.crawler_qa_tester.config;

import com.lmg.crawler_qa_tester.constants.AppConstant;
import com.lmg.crawler_qa_tester.dto.Domain;
import com.lmg.crawler_qa_tester.mapper.LinkEntityMapper;
import com.lmg.crawler_qa_tester.repository.entity.LinkEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class IntegrationConfig {
    @Autowired
    private Domain prodDomain;
    @Autowired
    private DataSource dataSource;

    @Bean
    public QueueChannel prodPollerChannel() {

        return new QueueChannel();
    }

    @Bean
    public PublishSubscribeChannel prodChannel() {

        return createChannel(prodDomain);
    }

    @Bean
    @InboundChannelAdapter(value = "prodPollerChannel",
        poller = @Poller(fixedRate = "${env.prod.pollerRate}"), autoStartup = "true")
    public MessageSource<?> prodMessagePoller() {

        JdbcPollingChannelAdapter jdbcPollingChannelAdapter =
            new JdbcPollingChannelAdapter(dataSource, getSelectSql(prodDomain));
        jdbcPollingChannelAdapter.setMaxRows(prodDomain.getConsumerThread());
        jdbcPollingChannelAdapter.setRowMapper(new LinkEntityMapper());
        jdbcPollingChannelAdapter.setUpdateSql(getUpdateSql(prodDomain.getName()));
        return jdbcPollingChannelAdapter;
    }

    @Splitter(inputChannel = "prodPollerChannel", outputChannel = "prodChannel")
    public List<Message<List<LinkEntity>>> split(List<LinkEntity> links) {

        return links.stream()
            .map(link -> MessageBuilder.withPayload(List.of(link)).build())
            .collect(Collectors.toList());
    }

    private PublishSubscribeChannel createChannel(Domain domain) {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(domain.getConsumerThread());
        executor.setMaxPoolSize(domain.getConsumerThread());
        executor.setThreadNamePrefix("TP_" + domain.getName());
        executor.initialize();
        return new PublishSubscribeChannel(executor);
    }

    private String getSelectSql(Domain domain) {

        if (AppConstant.PROD.equals(domain.getName())) {
            return
                "SELECT * from links where process_flag='N' and base_url = '"
                    + domain.getBaseUrl() + "' limit " + domain.getConsumerThread().toString();
        }
        return null;
    }

    private String getUpdateSql(String type) {

        if (AppConstant.PROD.equals(type)) {
            return "UPDATE links SET process_flag='Y' WHERE id = :id";
        }
        return null;
    }

}
