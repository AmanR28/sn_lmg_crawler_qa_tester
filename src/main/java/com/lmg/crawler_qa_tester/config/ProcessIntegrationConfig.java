package com.lmg.crawler_qa_tester.config;

import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import com.lmg.crawler_qa_tester.repository.mapper.CrawlDetailEntityMapper;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ProcessIntegrationConfig {
  @Value("${env.app.consumerThread}")
  private int CONSUMER_THREAD;

  @Value("${env.app.maxDepth}")
  private int MAX_DEPTH;

  @Autowired private DataSource dataSource;

  @Bean
  public PublishSubscribeChannel processPollerChannel() {

    return new PublishSubscribeChannel();
  }

  @Bean
  public PublishSubscribeChannel processConsumerChannel() {

    return createChannel();
  }

  @Bean
  @InboundChannelAdapter(
      value = "processPollerChannel",
      poller = @Poller(fixedRate = "10000"),
      autoStartup = "false")
  public MessageSource<?> processMessagePoller() {
    JdbcPollingChannelAdapter jdbcPollingChannelAdapter =
        new JdbcPollingChannelAdapter(dataSource, getSelectSql());
    jdbcPollingChannelAdapter.setRowMapper(new CrawlDetailEntityMapper());
    return jdbcPollingChannelAdapter;
  }

  @Transformer(inputChannel = "processPollerChannel", outputChannel = "processConsumerChannel")
  public Link transformToLinks(CrawlDetailEntity entity) {

    return new CrawlDetailEntityMapper().toLink(entity);
  }

  private PublishSubscribeChannel createChannel() {

    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(CONSUMER_THREAD);
    executor.setMaxPoolSize(CONSUMER_THREAD);
    executor.setThreadNamePrefix("TP_PROCESS");
    executor.initialize();
    return new PublishSubscribeChannel(executor);
  }

  private String getSelectSql() {

    return "SELECT * from crawl_detail where process_flag='"
        + LinkStatusEnum.NOT_PROCESSED
        + "' and depth < "
        + MAX_DEPTH
        + " limit "
        + CONSUMER_THREAD;
  }

  private String getUpdateSql() {

    return "UPDATE crawl_detail SET process_flag='"
        + LinkStatusEnum.IN_PROGRESS
        + "' WHERE id in (:id)";
  }
}
