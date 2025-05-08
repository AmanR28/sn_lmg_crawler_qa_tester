package com.lmg.crawler_qa_tester.repository.mapper;

import com.lmg.crawler_qa_tester.constants.ProcessStatusEnum;
import com.lmg.crawler_qa_tester.dto.Process;
import com.lmg.crawler_qa_tester.repository.entity.CrawlHeaderEntity;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class CrawlHeaderEntityMapper implements RowMapper<CrawlHeaderEntity> {

  @Override
  public CrawlHeaderEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
    CrawlHeaderEntity entity = new CrawlHeaderEntity();
    entity.setId(rs.getInt("id"));
    entity.setCompareFromBaseUrl(rs.getString("compare_from_base_url"));
    entity.setCompareToBaseUrl(rs.getString("compare_to_base_url"));
    entity.setStatus(rs.getString("status"));
    entity.setDomain(rs.getString("domain"));
    entity.setCountry(rs.getString("country"));
    entity.setLocale(rs.getString("locale"));
    entity.setDepartment(rs.getString("department"));
    entity.setCreatedTime(rs.getTimestamp("created_time"));
    entity.setUpdatedTime(rs.getTimestamp("updated_time"));
    entity.setConsumerThread(rs.getInt("consumer_thread"));
    entity.setPageCount(rs.getInt("page_count"));
    return entity;
  }

  public Process toProcess(CrawlHeaderEntity entity) {
    if (entity == null) {
      return null;
    }

    return Process.builder()
        .id(entity.getId())
        .compareFromBaseUrl(entity.getCompareFromBaseUrl())
        .compareToBaseUrl(entity.getCompareToBaseUrl())
        .status(
            entity.getStatus() != null
                ? ProcessStatusEnum.valueOf(entity.getStatus())
                : ProcessStatusEnum.NEW)
        .domain(entity.getDomain())
        .country(entity.getCountry())
        .locale(entity.getLocale())
        .department(entity.getDepartment())
        .pageCount(entity.getPageCount())
        .build();
  }

  public CrawlHeaderEntity fromProcess(Process process) {
    if (process == null) {
      return null;
    }

    CrawlHeaderEntity entity = new CrawlHeaderEntity();
    entity.setId(process.getId());
    entity.setCompareFromBaseUrl(process.getCompareFromBaseUrl());
    entity.setCompareToBaseUrl(process.getCompareToBaseUrl());
    entity.setStatus(
        process.getStatus() != null
            ? process.getStatus().getValue()
            : ProcessStatusEnum.NEW.getValue());
    entity.setDomain(process.getDomain());
    entity.setCountry(process.getCountry());
    entity.setLocale(process.getLocale());
    entity.setDepartment(process.getDepartment());
    return entity;
  }
}
