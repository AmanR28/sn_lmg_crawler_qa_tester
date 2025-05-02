package com.lmg.crawler_qa_tester.mapper;

import com.lmg.crawler_qa_tester.constants.LinkStatus;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Mapper(componentModel = "spring")
@Slf4j
public class LinkEntityMapper implements RowMapper<CrawlDetailEntity> {

    @Override
    public CrawlDetailEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        log.info("Mapping row {} from database", rowNum);
        CrawlDetailEntity entity = new CrawlDetailEntity();
        entity.setId(rs.getInt("id"));
        entity.setCrawlHeaderId(rs.getInt("project_id"));
        entity.setBaseUrl(rs.getString("base_url"));
        entity.setPath(rs.getString("url"));
        entity.setProcessFlag(LinkStatus.valueOf(rs.getString("process_flag")));
        log.info("Mapped Link: id={}, baseUrl={}, url={}", entity.getId(), entity.getBaseUrl(), entity.getPath());
        return entity;
    }

}

