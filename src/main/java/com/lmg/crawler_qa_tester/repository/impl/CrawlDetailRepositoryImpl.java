package com.lmg.crawler_qa_tester.repository.impl;

import com.lmg.crawler_qa_tester.repository.CrawlDetailRepository;
import com.lmg.crawler_qa_tester.repository.custom.CrawlerDetailRepositoryCustom;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CrawlDetailRepositoryImpl implements CrawlerDetailRepositoryCustom {
  @Autowired private JdbcTemplate jdbcTemplate;

  @Override
  public void saveNewLinks(List<CrawlDetailEntity> list) {
    if (list.isEmpty()) return;

    String sql =
        """
            INSERT INTO crawl_detail (crawl_header_id, env, base_url, path, process_flag, error_message, depth)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT ON CONSTRAINT unique_link DO NOTHING
        """;

    List<Object[]> args =
        list.stream()
            .map(
                e ->
                    new Object[] {
                      e.getCrawlHeaderId(),
                      e.getEnv(),
                      e.getBaseUrl(),
                      e.getPath(),
                      e.getProcessFlag(),
                      e.getErrorMessage(),
                            e.getDepth()
                    })
            .toList();

    jdbcTemplate.batchUpdate(sql, args);
  }
}
