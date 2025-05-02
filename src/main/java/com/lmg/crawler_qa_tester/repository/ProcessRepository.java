package com.lmg.crawler_qa_tester.repository;

import com.lmg.crawler_qa_tester.repository.entity.CrawlHeaderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessRepository extends JpaRepository<CrawlHeaderEntity, Integer> {
}
