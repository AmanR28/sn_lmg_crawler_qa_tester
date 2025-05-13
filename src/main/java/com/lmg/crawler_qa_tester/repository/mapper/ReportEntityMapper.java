package com.lmg.crawler_qa_tester.repository.mapper;

import com.lmg.crawler_qa_tester.repository.entity.ReportEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReportEntityMapper implements RowMapper<ReportEntity> {

    @Override
    public ReportEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ReportEntity.builder()
                .id(rs.getLong("id"))
                .host(rs.getString("host"))
                .country(rs.getString("country"))
                .locale(rs.getString("locale"))
                .department(rs.getString("department"))
                .crawlId(rs.getInt("crawl_id"))
                .status(rs.getString("status"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(rs.getTimestamp("updated_at") != null
                        ? rs.getTimestamp("updated_at").toLocalDateTime()
                        : null)
                .createdBy(rs.getString("created_by"))
                .updatedBy(rs.getString("updated_by"))
                .build();
    }
}
