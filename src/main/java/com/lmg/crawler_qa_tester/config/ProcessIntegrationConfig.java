package com.lmg.crawler_qa_tester.config;

import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import com.lmg.crawler_qa_tester.constants.ProcessStatusEnum;
import com.lmg.crawler_qa_tester.dto.Process;
import com.lmg.crawler_qa_tester.repository.entity.CrawlHeaderEntity;
import com.lmg.crawler_qa_tester.repository.mapper.CrawlHeaderEntityMapper;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.Router;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;

@Configuration
public class ProcessIntegrationConfig {
  @Value("${env.app.maxDepth}")
  private int MAX_DEPTH;

  @Autowired private DataSource dataSource;

  @Bean
  public PublishSubscribeChannel processPollerChannel() {
    return new PublishSubscribeChannel();
  }

  @Bean
  @InboundChannelAdapter(
      value = "processPollerChannel",
      poller = @Poller(fixedRate = "${env.app.pollerRate}"))
  public MessageSource<?> processMessagePoller() {
    JdbcPollingChannelAdapter jdbcPollingChannelAdapter =
        new JdbcPollingChannelAdapter(dataSource, getSelectSql());
    jdbcPollingChannelAdapter.setRowMapper(new CrawlHeaderEntityMapper());
    return jdbcPollingChannelAdapter;
  }

  private String getSelectSql() {
    return "( SELECT * FROM crawl_header WHERE status = '"
        + ProcessStatusEnum.NEW
        + "' ORDER BY id LIMIT 1 ) UNION ALL ( SELECT * FROM crawl_header WHERE status = '"
        + ProcessStatusEnum.RUNNING
        + "' ORDER BY id LIMIT 1 )";
  }

  @Bean
  public PublishSubscribeChannel processRouterChannel() {
    return new PublishSubscribeChannel();
  }

  @Transformer(inputChannel = "processPollerChannel", outputChannel = "processRouterChannel")
  public List<Process> transformToProcesses(List<CrawlHeaderEntity> entity) {
    return entity.stream().map(e -> new CrawlHeaderEntityMapper().toProcess(e)).toList();
  }

  @Bean
  public PublishSubscribeChannel newProcessConsumerChannel() {
    return new PublishSubscribeChannel();
  }

  @Bean
  public PublishSubscribeChannel runningProcessConsumerChannel() {
    return new PublishSubscribeChannel();
  }

  @Router(inputChannel = "processRouterChannel")
  public String processRouter(List<Process> processes) {
    if (processes == null || processes.isEmpty()) return null;
    if (processes.size() != 1) return null;
    Process process = processes.get(0);
    ProcessStatusEnum status = process.getStatus();
    if (status == ProcessStatusEnum.RUNNING) {
      return "runningProcessConsumerChannel";
    }
    if (status == ProcessStatusEnum.NEW) {
      return "newProcessConsumerChannel";
    }
    return null;
  }

  @Bean
  public PublishSubscribeChannel completeProcessPollerChannel() {
    return new PublishSubscribeChannel();
  }

  @Bean
  @InboundChannelAdapter(
      value = "completeProcessPollerChannel",
      poller = @Poller(fixedRate = "${env.app.pollerRate}"))
  public MessageSource<?> completeProcessMessagePoller() {
    JdbcPollingChannelAdapter jdbcPollingChannelAdapter =
        new JdbcPollingChannelAdapter(dataSource, getSelectSqlForCompleteProcess());
    jdbcPollingChannelAdapter.setRowMapper(
        (rs, rowNum) -> {
          return rs.getInt(1);
        });
    return jdbcPollingChannelAdapter;
  }

  private String getSelectSqlForCompleteProcess() {
    return "( SELECT COUNT(*) FROM crawl_detail WHERE (process_flag = '"
        + LinkStatusEnum.NOT_PROCESSED
        + "' OR process_flag = '"
        + LinkStatusEnum.IN_PROGRESS
        + "') AND depth < "
        + MAX_DEPTH
        + " LIMIT 1 )";
  }
}
