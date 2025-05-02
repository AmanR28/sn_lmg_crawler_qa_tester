package com.lmg.crawler_qa_tester.repository;

import com.lmg.crawler_qa_tester.repository.entity.LinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository extends JpaRepository<LinkEntity, Integer> {
}
