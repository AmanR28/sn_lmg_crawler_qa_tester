package com.lmg.crawler_qa_tester.repository.impl;

import com.lmg.crawler_qa_tester.repository.DomainRepository;
import com.lmg.crawler_qa_tester.repository.entity.DomainDepartmentEntity;
import com.lmg.crawler_qa_tester.repository.internal.DomainDepartment;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DomainRepositoryImpl implements DomainRepository {
  @Autowired private DomainDepartment domainDepartment;

  public List<String> getDepartments(String domain) {
    DomainDepartmentEntity entity = domainDepartment.getReferenceById(domain);
    return Arrays.stream(entity.getDepartments().split(",")).toList();
  }
}
