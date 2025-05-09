package com.lmg.crawler_qa_tester.repository.internal;

import com.lmg.crawler_qa_tester.dto.Link;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrawlDetailRepository extends JpaRepository<CrawlDetailEntity, Integer> {
    List<CrawlDetailEntity> getCrawlDetailEntitiesByCrawlHeaderId(Integer processId);
}
