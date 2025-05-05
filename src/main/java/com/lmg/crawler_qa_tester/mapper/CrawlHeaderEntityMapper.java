package com.lmg.crawler_qa_tester.mapper;

import com.lmg.crawler_qa_tester.constants.ConsumerStatusEnum;
import com.lmg.crawler_qa_tester.dto.Process;
import com.lmg.crawler_qa_tester.repository.entity.CrawlHeaderEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CrawlHeaderEntityMapper implements RowMapper<CrawlHeaderEntity> {

    @Override
    public CrawlHeaderEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        CrawlHeaderEntity entity = new CrawlHeaderEntity();
        entity.setId(rs.getInt("id"));
        entity.setProdBaseUrl(rs.getString("prod_base_url"));
        entity.setPreProdBaseUrl(rs.getString("pre_prod_base_url"));
        entity.setStatus(rs.getString("status"));
        entity.setCreatedTime(rs.getTimestamp("created_time"));
        entity.setUpdatedTime(rs.getTimestamp("updated_time"));
        return entity;
    }

    public Process toProcess(CrawlHeaderEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return Process.builder()
            .id(entity.getId())
            .prodBaseUrl(entity.getProdBaseUrl())
            .preProdBaseUrl(entity.getPreProdBaseUrl())
            .status(entity.getStatus() != null ? ConsumerStatusEnum.valueOf(entity.getStatus()) : ConsumerStatusEnum.INIT)
            .createdTime(entity.getCreatedTime())
            .updatedTime(entity.getUpdatedTime())
            .build();
    }

    public CrawlHeaderEntity fromProcess(Process process) {
        if (process == null) {
            return null;
        }
        
        CrawlHeaderEntity entity = new CrawlHeaderEntity();
        entity.setId(process.getId());
        entity.setProdBaseUrl(process.getProdBaseUrl());
        entity.setPreProdBaseUrl(process.getPreProdBaseUrl());
        entity.setStatus(process.getStatus() != null ? process.getStatus().getValue() : ConsumerStatusEnum.INIT.getValue());
        return entity;
    }
}
