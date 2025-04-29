package com.lmg.crawler_qa_tester.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import com.lmg.crawler_qa_tester.model.Domain;
import com.lmg.crawler_qa_tester.model.mapper.LinkEntityMapper;

import javax.sql.DataSource;
import java.sql.SQLException;

import static com.lmg.crawler_qa_tester.util.Constants.PRE_PROD_CHANNEL;
import static com.lmg.crawler_qa_tester.util.Constants.PROD_CHANNEL;

@Configuration
@RequiredArgsConstructor
public class IntegrationConfig {
    private final DataSource dataSource;
    @Qualifier("prodDomain")
    private final Domain prodDomain;
    @Qualifier("preProdDomain")
    private final Domain preProdDomain;

    private String getSelectQuery(String type) {

        return "SELECT id, url, processed, status, type from links where processed='N' and type='"
            + type + "' LIMIT 1";
    }

    private String getUpdateQuery(String type) {

        return "UPDATE links SET processed='Y' WHERE id IN (:ids) and type='" + type + "'";
    }

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
    @InboundChannelAdapter(value = PROD_CHANNEL, poller = @Poller(fixedRate = "50"),
        autoStartup = "true")
    public MessageSource<?> prodChannelAdapter() throws SQLException {

        JdbcPollingChannelAdapter jdbcPollingChannelAdapter =
            new JdbcPollingChannelAdapter(dataSource, getSelectQuery("PROD"));
        jdbcPollingChannelAdapter.setRowMapper(new LinkEntityMapper());
        jdbcPollingChannelAdapter.setUpdateSql(
            getUpdateQuery("PROD"));
        return jdbcPollingChannelAdapter;
    }

    @Bean
    @InboundChannelAdapter(value = PRE_PROD_CHANNEL, poller = @Poller(fixedRate = "50"),
        autoStartup = "true")
    public MessageSource<?> preProdChannelAdapter() throws SQLException {

        JdbcPollingChannelAdapter jdbcPollingChannelAdapter =
            new JdbcPollingChannelAdapter(dataSource, getSelectQuery("PRE_PROD"));
        jdbcPollingChannelAdapter.setRowMapper(new LinkEntityMapper());
        jdbcPollingChannelAdapter.setUpdateSql(getUpdateQuery("PRE_PROD"));
        return jdbcPollingChannelAdapter;
    }

}
