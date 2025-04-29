package com.lmg.crawler_qa_tester.config;

import com.lmg.crawler_qa_tester.model.Domain;
import com.lmg.crawler_qa_tester.model.mapper.LinkEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.sql.SQLException;

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

    @Bean
    @InboundChannelAdapter(value = "prodChannel", poller = @Poller(fixedRate = "5000"),
        autoStartup = "true")
    public MessageSource<?> storedProc(DataSource dataSource) throws SQLException {

        JdbcPollingChannelAdapter jdbcPollingChannelAdapter =
            new JdbcPollingChannelAdapter(dataSource,
                "SELECT id, url, processed, status, type from links where processed='N' and type = 'PROD' limit 1");
        jdbcPollingChannelAdapter.setRowMapper(new LinkEntityMapper());
        jdbcPollingChannelAdapter.setUpdateSql(
            "UPDATE links SET processed='Y' WHERE id = :id");
        return jdbcPollingChannelAdapter;

    }

}
