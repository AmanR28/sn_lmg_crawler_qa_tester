package com.lmg.crawler_qa_tester.config;

import com.lmg.crawler_qa_tester.constants.ProcessStatusEnum;
import com.lmg.crawler_qa_tester.constants.ReportStatus;
import com.lmg.crawler_qa_tester.dto.ReportDetails;
import com.lmg.crawler_qa_tester.repository.ReportRepository;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import com.lmg.crawler_qa_tester.repository.entity.CrawlHeaderEntity;
import com.lmg.crawler_qa_tester.repository.entity.ReportEntity;
import com.lmg.crawler_qa_tester.repository.internal.CrawlDetailRepository;
import com.lmg.crawler_qa_tester.repository.internal.CrawlHeaderRepository;
import com.lmg.crawler_qa_tester.repository.mapper.CrawlDetailEntityMapper;
import com.lmg.crawler_qa_tester.repository.mapper.CrawlHeaderEntityMapper;
import com.lmg.crawler_qa_tester.repository.mapper.ReportEntityMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class ReportIntegrationConfig {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private CrawlHeaderRepository crawlHeaderRepository;
    @Autowired
    private CrawlDetailRepository crawlDetailRepository;
    @Autowired
    private ReportRepository reportRepository;
    @Bean
    @InboundChannelAdapter(
            value = "reportPollerChannel",
            poller = @Poller(fixedRate = "${env.app.pollerRate}"))
    public MessageSource<?> reportMessagePoller() {
        JdbcPollingChannelAdapter jdbcPollingChannelAdapter =
                new JdbcPollingChannelAdapter(dataSource, getSelectSql());
        jdbcPollingChannelAdapter.setRowMapper(new ReportEntityMapper());
        return jdbcPollingChannelAdapter;
    }
    private String getSelectSql() {
        return "( SELECT * FROM report WHERE status = '" + ReportStatus.NOT_AVAILABLE.getCode()+
                "' ORDER BY id LIMIT 1)";
    }
    @Bean
    public PublishSubscribeChannel reportProcessorChannel() {
        return new PublishSubscribeChannel();
    }

        @Transactional
        @Transformer(inputChannel = "reportPollerChannel", outputChannel = "reportProcessorChannel")
        public ReportDetails getReportWithCrawler(Message<ReportEntity> message) {
            ReportEntity reportEntity = message.getPayload();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            Long id = reportEntity.getId();
            Integer crawlHeaderId = reportEntity.getCrawlId();
//            //String crawlHeaderSqlCheck = "SELECT * FROM crawl_header WHERE id = ? and status = ?";
//            List<CrawlHeaderEntity> crawlHeaderEntities = crawlHeaderRepository.findAllByIdAndStatus(reportEntity.getId(),ProcessStatusEnum.COMPLETED.getValue());
//            if(crawlHeaderEntities.isEmpty())
//            {
//                return null;
//            }
            List<CrawlDetailEntity> crawlDetailEntities = crawlDetailRepository.findAllByCrawlHeaderId(crawlHeaderId);
            reportEntity.setStatus(ReportStatus.IN_PROGRESS.getCode());
            reportEntity.setUpdatedAt(LocalDateTime.now());
            reportRepository.save(reportEntity);
            return new ReportDetails(reportEntity.getId(),crawlDetailEntities,reportEntity.getLocale(),reportEntity.getCountry());
        }

}
