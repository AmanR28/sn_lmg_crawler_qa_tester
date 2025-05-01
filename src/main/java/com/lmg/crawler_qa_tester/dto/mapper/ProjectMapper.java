package com.lmg.crawler_qa_tester.dto.mapper;

import com.lmg.crawler_qa_tester.dto.Project;
import com.lmg.crawler_qa_tester.repository.entity.ProjectEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    Project toDto(ProjectEntity entity);
    ProjectEntity toEntity(Project dto);
}
