DELETE FROM crawl_header;
DELETE FROM crawl_detail;

INSERT INTO domain_department (domain, departments)
VALUES ('centrepointstores.com', 'men')
ON CONFLICT DO NOTHING;
