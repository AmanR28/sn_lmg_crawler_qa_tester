package com.lmg.crawler_qa_tester.dto.mapper;

import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.repository.entity.LinkEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LinkMapper {
    Link toDto(LinkEntity entity);
    LinkEntity toEntity(Link dto);
}

