package com.lmg.crawler_qa_tester.repository;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface DomainRepository {
  List<String> getDepartments(String domain);
}
