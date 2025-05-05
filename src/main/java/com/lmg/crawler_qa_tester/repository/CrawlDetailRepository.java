package com.lmg.crawler_qa_tester.repository;

import com.lmg.crawler_qa_tester.repository.custom.CrawlerDetailRepositoryCustom;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrawlDetailRepository
    extends JpaRepository<CrawlDetailEntity, Integer>, CrawlerDetailRepositoryCustom {
  void saveNewLinks(List<CrawlDetailEntity> crawlDetailEntityList);
    List<CrawlDetailEntity> findByCrawlHeaderId(Integer crawlHeaderId);
}
