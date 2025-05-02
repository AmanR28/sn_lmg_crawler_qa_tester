package com.lmg.crawler_qa_tester.mapper;

import com.lmg.crawler_qa_tester.constants.LinkStatus;
import com.lmg.crawler_qa_tester.repository.entity.LinkEntity;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Mapper(componentModel = "spring")
@Slf4j
public class LinkEntityMapper implements RowMapper<LinkEntity> {

    @Override
    public LinkEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        log.info("Mapping row {} from database", rowNum);
        LinkEntity entity = new LinkEntity();
        entity.setId(rs.getInt("id"));
        entity.setProjectId(rs.getInt("project_id"));
        entity.setBaseUrl(rs.getString("base_url"));
        entity.setUrl(rs.getString("url"));
        entity.setProcessFlag(rs.getString("process_flag"));
        try {
            entity.setLinkStatus(LinkStatus.valueOf(rs.getString("link_status")));
        } catch (Exception e) {
            log.warn("Could not map link_status, using default NEW", e);
            entity.setLinkStatus(LinkStatus.NEW);
        }
        log.info("Mapped Link: id={}, baseUrl={}, url={}", entity.getId(), entity.getBaseUrl(), entity.getUrl());
        return entity;
    }

}

