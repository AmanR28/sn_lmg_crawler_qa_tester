package com.lmg.crawler_qa_tester.repository.internal;

import com.lmg.crawler_qa_tester.repository.entity.DomainDepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainDepartment extends JpaRepository<DomainDepartmentEntity, String> {}
