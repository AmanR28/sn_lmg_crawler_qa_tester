package com.lmg.crawler_qa_tester.model.mapper;

import org.springframework.jdbc.core.RowMapper;
import com.lmg.crawler_qa_tester.model.LinkEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LinkEntityMapper implements RowMapper<LinkEntity> {

    @Override
    public LinkEntity mapRow(ResultSet rs, int rowNum) throws SQLException {

        return LinkEntity.builder().id(rs.getInt("id")).url(rs.getString("url"))
            .processed(rs.getString("processed")).type(rs.getString("type"))
            .status(rs.getString("status")).build();
    }

}
