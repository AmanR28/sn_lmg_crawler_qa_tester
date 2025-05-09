package com.lmg.crawler_qa_tester.repository.impl;

import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.dto.Process;
import com.lmg.crawler_qa_tester.repository.CrawlRepository;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import com.lmg.crawler_qa_tester.repository.entity.CrawlHeaderEntity;
import com.lmg.crawler_qa_tester.repository.internal.CrawlDetailRepository;
import com.lmg.crawler_qa_tester.repository.internal.CrawlHeaderRepository;
import com.lmg.crawler_qa_tester.repository.mapper.CrawlDetailEntityMapper;
import com.lmg.crawler_qa_tester.repository.mapper.CrawlHeaderEntityMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CrawlRepositoryImpl implements CrawlRepository {

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private CrawlDetailRepository crawlDetailRepository;
  @Autowired private CrawlHeaderRepository crawlHeaderRepository;

  @Override
  public boolean hasActiveProcess() {
    return crawlHeaderRepository.hasActiveProcess();
  }

  @Override
  public void saveProcess(Process process) {
    crawlHeaderRepository.save(toCrawlHeaderEntity(process));
  }

  @Override
  public void saveAllProcesses(List<Process> processes) {
    crawlHeaderRepository.saveAll(processes.stream().map(this::toCrawlHeaderEntity).toList());
  }

  @Override
  public void saveLink(Link link) {
    crawlDetailRepository.save(toCrawlDetailEntity(link));
  }

  @Override
  public List<Link> getLinksByProcessId(Integer processId) {
    return crawlDetailRepository.getCrawlDetailEntitiesByCrawlHeaderId(processId).stream()
        .map(this::toLink)
        .toList();
  }

  @Override
  public void saveNewLinks(List<Link> links) {
    if (links.isEmpty()) return;

    String sql =
        """
                    INSERT INTO crawl_detail (crawl_header_id, env, base_url, path, process_flag, error_message, depth)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT ON CONSTRAINT unique_link DO NOTHING
                """;

    List<Object[]> args =
        links.stream()
            .map(
                e ->
                    new Object[] {
                      e.getCrawlHeaderId(),
                      e.getEnv().getValue(),
                      e.getBaseUrl(),
                      e.getPath(),
                      e.getProcessFlag().getValue(),
                      e.getErrorMessage(),
                      e.getDepth()
                    })
            .toList();

    jdbcTemplate.batchUpdate(sql, args);
  }

  private CrawlHeaderEntity toCrawlHeaderEntity(Process process) {
    return new CrawlHeaderEntityMapper().fromProcess(process);
  }

  private CrawlDetailEntity toCrawlDetailEntity(Link link) {
    return new CrawlDetailEntityMapper().fromLink(link);
  }

  private Process toProcess(CrawlHeaderEntity crawlHeaderEntity) {
    return new CrawlHeaderEntityMapper().toProcess(crawlHeaderEntity);
  }

  private Link toLink(CrawlDetailEntity entity) {
    return new CrawlDetailEntityMapper().toLink(entity);
  }
}
