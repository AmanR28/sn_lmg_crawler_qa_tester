package com.lmg.crawler_qa_tester.repository;

import com.lmg.crawler_qa_tester.model.mapper.LinkEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.web.Link;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.lmg.crawler_qa_tester.model.LinkEntity;

import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class LinkRepository {
    private final JdbcTemplate jdbcTemplate;

    public void regenerateTable() {

        String dropTable = "DROP TABLE IF EXISTS links";
        jdbcTemplate.execute(dropTable);
        String createTable =
            "CREATE TABLE links (id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, url VARCHAR(255), processed VARCHAR(255), type VARCHAR(255), status VARCHAR(255))";
        jdbcTemplate.execute(createTable);
    }

    public LinkEntity getAnUnprocessedLink(String type) {

        String sql =
            "SELECT id, url, processed, type, status FROM links WHERE processed = 'N' and type = ? LIMIT 1";
        LinkEntity link = jdbcTemplate.queryForObject(sql, new LinkEntityMapper(), type);
        log.debug("SQL getAnUnprocessedLink: {}", link);
        return link;
    }

    public void updateLink(LinkEntity link) {

        String sql = "UPDATE links SET processed = ?, status = ? WHERE id = ?";
        jdbcTemplate.update(sql, link.getProcessed(), link.getStatus(), link.getId());
    }

    public void saveLink(LinkEntity link) {

        String sql = "INSERT INTO links (url, processed, type, status) VALUES (?, ?, ?, ?)";
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

    public void saveLinks(List<LinkEntity> links) {

        String sql = "INSERT INTO links (url, processed, type, status) " +
            "SELECT ?, ?, ?, ? " +
            "WHERE NOT EXISTS (SELECT 1 FROM links WHERE url = ?)";

        for (LinkEntity link : links) {
            try {
                jdbcTemplate.update(sql,
                    link.getUrl(),
                    link.getProcessed(),
                    link.getType(),
                    link.getStatus(),
                    link.getUrl());
                log.debug("Saved link: {}", link.getUrl());
            } catch (Exception e) {
                log.error("Error saving link: {}", link.getUrl(), e);
            }
        }

    }

}
