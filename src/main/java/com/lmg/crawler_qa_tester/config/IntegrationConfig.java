package com.lmg.crawler_qa_tester.config;

import com.lmg.crawler_qa_tester.constants.EnvironmentEnum;
import com.lmg.crawler_qa_tester.constants.LinkStatus;
import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.mapper.CrawlDetailEntityMapper;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.*;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class IntegrationConfig {
    private final int CONSUMER_THREAD =
        Integer.parseInt(System.getProperty("env.app.consumerThread", "1"));

    @Autowired
    private DataSource dataSource;

    @Bean
    public PublishSubscribeChannel pollerChannel() {

        return new PublishSubscribeChannel();
    }

    @Bean
    public PublishSubscribeChannel transformChannel() {

        return new PublishSubscribeChannel();
    }

    @Bean
    public PublishSubscribeChannel routerChannel() {

        return new PublishSubscribeChannel();
    }

    @Bean
    public PublishSubscribeChannel prodChannel() {

        return createChannel(EnvironmentEnum.PROD.getValue());
    }

    @Bean
    public PublishSubscribeChannel preProdChannel() {

        return createChannel(EnvironmentEnum.PRE_PROD.getValue());
    }

    @Bean
    @InboundChannelAdapter(value = "pollerChannel",
        poller = @Poller(fixedRate = "${env.app.pollerRate}"), autoStartup = "false")
    public MessageSource<?> messagePoller() {

        JdbcPollingChannelAdapter jdbcPollingChannelAdapter =
            new JdbcPollingChannelAdapter(dataSource, getSelectSql());
        jdbcPollingChannelAdapter.setMaxRows(CONSUMER_THREAD);
        jdbcPollingChannelAdapter.setRowMapper(new CrawlDetailEntityMapper());
        jdbcPollingChannelAdapter.setUpdateSql(getUpdateSql());
        return jdbcPollingChannelAdapter;
    }

    @Splitter(inputChannel = "pollerChannel", outputChannel = "transformChannel")
    public List<Message<CrawlDetailEntity>> split(List<CrawlDetailEntity> links) {

        return links.stream().map(link -> MessageBuilder.withPayload(link).build())
            .collect(Collectors.toList());
    }

    @Transformer(inputChannel = "transformChannel", outputChannel = "routerChannel")
    public Link transformToLinks(CrawlDetailEntity entity) {

        return new CrawlDetailEntityMapper().toLink(entity);
    }

    @Router(inputChannel = "routerChannel")
    public String routeByEnvironment(Link link) {

        if (EnvironmentEnum.PROD.equals(link.getEnv())) {
            return "prodChannel";
        } else
            if (EnvironmentEnum.PRE_PROD.equals(link.getEnv())) {
                return "preProdChannel";
            }
        return null;
    }

    private PublishSubscribeChannel createChannel(String name) {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CONSUMER_THREAD);
        executor.setMaxPoolSize(CONSUMER_THREAD);
        executor.setThreadNamePrefix("TP_" + name);
        executor.initialize();
        return new PublishSubscribeChannel(executor);
    }

    private String getSelectSql() {

        return "SELECT * from crawl_detail where process_flag='" + LinkStatus.NOT_PROCESSED
            + "' limit " + String.valueOf(CONSUMER_THREAD);
    }

    private String getUpdateSql() {

        return "UPDATE crawl_detail SET process_flag='" + LinkStatus.IN_PROGRESS
            + "' WHERE id = :id";
    }

}
