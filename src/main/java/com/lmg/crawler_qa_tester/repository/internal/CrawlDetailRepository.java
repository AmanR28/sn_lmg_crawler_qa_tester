package com.lmg.crawler_qa_tester.repository.internal;

import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlDetailRepository extends JpaRepository<CrawlDetailEntity, Integer> {

  void saveNewLinks(List<CrawlDetailEntity> crawlDetailEntityList);

  List<CrawlDetailEntity> findAllByCrawlHeaderId(Integer crawlHeaderId);
}
