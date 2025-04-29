package com.lmg.crawler_qa_tester.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.lmg.crawler_qa_tester.model.LinkEntity;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class LinkRepository {
    private final JdbcTemplate jdbcTemplate;

    public void saveLinks(List<LinkEntity> links) {
        if (links == null || links.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO links (url, processed, type, status) VALUES (?, ?, ?, ?)";
        
        for (LinkEntity link : links) {
            try {
                jdbcTemplate.update(sql, 
                    link.getUrl(), 
                    link.getProcessed(), 
                    link.getType(),
                    link.getStatus());
                log.debug("Saved link: {}", link.getUrl());
            } catch (Exception e) {
                log.error("Error saving link: {}", link.getUrl(), e);
            }
        }
    }
}
