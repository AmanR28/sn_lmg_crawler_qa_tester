package com.lmg.crawler_qa_tester.repository.impl;

import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import com.lmg.crawler_qa_tester.constants.ProcessStatusEnum;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CrawlRepositoryImpl implements CrawlRepository {
  @Value("${env.app.maxDepth}")
  private int MAX_DEPTH;

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private CrawlDetailRepository crawlDetailRepository;
  @Autowired private CrawlHeaderRepository crawlHeaderRepository;

  @Override
  public boolean hasActiveProcess() {
    return crawlHeaderRepository.hasActiveProcess();
  }

  @Override
  public Process saveProcess(Process process) {
    return new CrawlHeaderEntityMapper()
        .toProcess(crawlHeaderRepository.save(toCrawlHeaderEntity(process)));
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
                    INSERT INTO crawl_detail (crawl_header_id, env, base_url, path, process_flag, error_message, depth, parent_path)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT ON CONSTRAINT unique_link DO NOTHING
                """;

    int BATCH_SIZE = 20;
    for (int i = 0; i < links.size(); i += BATCH_SIZE) {
      int end = Math.min(i + BATCH_SIZE, links.size());
      List<Object[]> batch =
          links.subList(i, end).stream()
              .map(
                  e ->
                      new Object[] {
                        e.getCrawlHeaderId(),
                        e.getEnv().getValue(),
                        e.getBaseUrl(),
                        e.getPath(),
                        e.getProcessFlag().getValue(),
                        e.getErrorMessage(),
                        e.getDepth(),
                        e.getParentPath()
                      })
              .toList();
      jdbcTemplate.batchUpdate(sql, batch);
    }
  }

  @Override
  public void resetInProgressLinks(Integer id, LinkStatusEnum fromFlag, LinkStatusEnum toFlag) {
    crawlDetailRepository.batchUpdateProgressFlag(id, fromFlag.getValue(), toFlag.getValue());
  }

  @Override
  public Process getProcessByStatus(ProcessStatusEnum status) {
    return toProcess(crawlHeaderRepository.findByStatus(status.getValue()));
  }

  @Override
  public int getLinkCountByProcessId(Integer processId) {
    String sql =
        """
        SELECT COUNT(*) FROM crawl_detail
        WHERE crawl_header_id = ? AND depth <= ?
    """;
    return jdbcTemplate.queryForObject(sql, Integer.class, processId, MAX_DEPTH);
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
