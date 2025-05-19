-- delete from crawl_header;
-- delete from crawl_detail;
 DELETE FROM report;
INSERT INTO domain_department (domain, departments)
VALUES ('maxfashion.com', 'men')
ON CONFLICT DO NOTHING;
