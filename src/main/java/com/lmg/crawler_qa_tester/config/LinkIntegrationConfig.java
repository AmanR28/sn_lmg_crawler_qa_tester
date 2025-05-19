package com.lmg.crawler_qa_tester.config;

import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import com.lmg.crawler_qa_tester.repository.mapper.CrawlDetailEntityMapper;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.*;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@Slf4j
public class LinkIntegrationConfig {
  @Value("${env.app.consumerThread}")
  private int CONSUMER_THREAD;

  @Value("${env.app.maxDepth}")
  private int MAX_DEPTH;

  @Autowired private DataSource dataSource;

  @Bean
  public PublishSubscribeChannel linkPollerChannel() {

    return new PublishSubscribeChannel();
  }

  @Bean
  public PublishSubscribeChannel linkTransformChannel() {

    return new PublishSubscribeChannel();
  }

  @Bean
  public PublishSubscribeChannel linkConsumerChannel() {

    return createChannel();
  }

  @Bean
  @InboundChannelAdapter(
      value = "linkPollerChannel",
      poller = @Poller(fixedRate = "${env.app.pollerRate}"),
      autoStartup = "false")
  public MessageSource<?> linkMessagePoller() {
    JdbcPollingChannelAdapter jdbcPollingChannelAdapter =
        new JdbcPollingChannelAdapter(dataSource, getSelectSql());
    jdbcPollingChannelAdapter.setMaxRows(CONSUMER_THREAD);
    jdbcPollingChannelAdapter.setRowMapper(new CrawlDetailEntityMapper());
    jdbcPollingChannelAdapter.setUpdateSql(getUpdateSql());
    return jdbcPollingChannelAdapter;
  }

  @Splitter(inputChannel = "linkPollerChannel", outputChannel = "linkTransformChannel")
  public List<Message<CrawlDetailEntity>> split(List<CrawlDetailEntity> links) {
    return links.stream()
        .map(link -> MessageBuilder.withPayload(link).build())
        .collect(Collectors.toList());
  }

  @Transformer(inputChannel = "linkTransformChannel", outputChannel = "linkConsumerChannel")
  public Link transformToLinks(CrawlDetailEntity entity) {

    return new CrawlDetailEntityMapper().toLink(entity);
  }

  private PublishSubscribeChannel createChannel() {

    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(CONSUMER_THREAD);
    executor.setMaxPoolSize(CONSUMER_THREAD);
    executor.setThreadNamePrefix("TP_LINK_");
    executor.initialize();
    return new PublishSubscribeChannel(executor);
  }

  private String getSelectSql() {

    return "SELECT * from crawl_detail where process_flag='"
        + LinkStatusEnum.NOT_PROCESSED
        + "' and depth <= "
        + MAX_DEPTH
        + " limit "
        + 1;
  }

  private String getUpdateSql() {

    return "UPDATE crawl_detail SET process_flag='"
        + LinkStatusEnum.IN_PROGRESS
        + "' WHERE id in (:id)";
  }
}
