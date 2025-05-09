package com.lmg.crawler_qa_tester.config;

import com.lmg.crawler_qa_tester.constants.ProcessStatusEnum;
import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.dto.Process;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import com.lmg.crawler_qa_tester.repository.entity.CrawlHeaderEntity;
import com.lmg.crawler_qa_tester.repository.mapper.CrawlDetailEntityMapper;
import javax.sql.DataSource;

import com.lmg.crawler_qa_tester.repository.mapper.CrawlHeaderEntityMapper;
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

    return new PublishSubscribeChannel();
  }

  @Bean
  @InboundChannelAdapter(value = "processPollerChannel", poller = @Poller(fixedRate = "1000"))
  public MessageSource<?> processMessagePoller() {
    JdbcPollingChannelAdapter jdbcPollingChannelAdapter =
        new JdbcPollingChannelAdapter(dataSource, getSelectSql());
    jdbcPollingChannelAdapter.setRowMapper(new CrawlHeaderEntityMapper());
    return jdbcPollingChannelAdapter;
  }

  @Transformer(inputChannel = "processPollerChannel", outputChannel = "processConsumerChannel")
  public Process transformToLinks(CrawlHeaderEntity entity) {

    return new CrawlHeaderEntityMapper().toProcess(entity);
  }

  private String getSelectSql() {
    return " SELECT * FROM crawl_header WHERE status = '"
        + ProcessStatusEnum.NEW
        + "' AND NOT EXISTS ( SELECT 1 FROM crawl_header p2 WHERE p2.status = '"
        + ProcessStatusEnum.RUNNING
        + "' ) LIMIT 1";
  }
}
