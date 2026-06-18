SET @index_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'post'
      AND index_name = 'ft_post_content'
);

SET @ddl = IF(
    @index_exists = 0,
    'ALTER TABLE post ADD FULLTEXT INDEX ft_post_content (content)',
    'DO 0'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
