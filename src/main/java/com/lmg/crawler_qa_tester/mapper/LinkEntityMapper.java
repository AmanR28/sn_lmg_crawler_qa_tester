package com.lmg.crawler_qa_tester.mapper;

import com.lmg.crawler_qa_tester.repository.entity.LinkEntity;
import org.mapstruct.Mapper;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;

@Mapper(componentModel = "spring")
public class LinkEntityMapper implements RowMapper<LinkEntity> {

    @Override
    public LinkEntity mapRow(ResultSet rs, int rowNum) {
        return null;
    }

}

