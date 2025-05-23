package com.lmg.crawler_qa_tester.repository.mapper;

import com.lmg.crawler_qa_tester.constants.EnvironmentEnum;
import com.lmg.crawler_qa_tester.constants.LinkStatusEnum;
import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

@Slf4j
public class CrawlDetailEntityMapper implements RowMapper<CrawlDetailEntity> {

  @Override
  public CrawlDetailEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
    log.debug("Mapping DetailEntity {}", rs.getInt("id"));

    CrawlDetailEntity entity = new CrawlDetailEntity();
    entity.setId(rs.getInt("id"));
    entity.setCrawlHeaderId(rs.getInt("crawl_header_id"));
    entity.setEnv(rs.getString("env"));
    entity.setBaseUrl(rs.getString("base_url"));
    entity.setPath(rs.getString("path"));
    entity.setProcessFlag(rs.getString("process_flag"));
    entity.setDepth(rs.getInt("depth"));
    return entity;
  }

  public Link toLink(CrawlDetailEntity entity) {

    if (entity == null) {
      return null;
    }

    return Link.builder()
        .id(entity.getId())
        .crawlHeaderId(entity.getCrawlHeaderId())
        .baseUrl(entity.getBaseUrl())
        .path(entity.getPath())
        .depth(entity.getDepth())
        .env(EnvironmentEnum.valueOf(entity.getEnv()))
        .processFlag(LinkStatusEnum.valueOf(entity.getProcessFlag()))
        .errorMessage(entity.getErrorMessage())
        .parentPath(entity.getParentPath())
        .depth(entity.getDepth())
        .productCount(entity.getProductCount())
        .build();
  }

  public CrawlDetailEntity fromLink(Link link) {

    if (link == null) {
      return null;
    }

    CrawlDetailEntity entity = new CrawlDetailEntity();
    entity.setId(link.getId());
    entity.setCrawlHeaderId(link.getCrawlHeaderId());
    entity.setBaseUrl(link.getBaseUrl());
    entity.setEnv(link.getEnv().getValue());
    entity.setPath(link.getPath());
    entity.setDepth(link.getDepth());
    entity.setProcessFlag(
        link.getProcessFlag() != null
            ? link.getProcessFlag().getValue()
            : LinkStatusEnum.NOT_PROCESSED.getValue());
    entity.setParentPath(link.getParentPath());
    entity.setErrorMessage(link.getErrorMessage());
    entity.setDepth(link.getDepth());
    entity.setProductCount(link.getProductCount());
    return entity;
  }
}
