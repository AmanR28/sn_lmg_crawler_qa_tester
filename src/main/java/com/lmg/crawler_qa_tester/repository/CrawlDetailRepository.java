package com.lmg.crawler_qa_tester.repository;

import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlDetailRepository extends JpaRepository<CrawlDetailEntity, Integer> {}
