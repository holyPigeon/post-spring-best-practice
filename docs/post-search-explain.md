# Post Search EXPLAIN Notes

## Indexes

- `idx_post_created_at_id (created_at, id)`: supports default latest/oldest offset paging with a stable id tie-breaker.
- `idx_post_author_id_created_at_id (author_id, created_at, id)`: supports equality filtering by author and ordering/range checks by created time with a stable id tie-breaker.

## Queries To Record In PR

Default latest page:

```sql
EXPLAIN
SELECT p.*
FROM post p
ORDER BY p.created_at DESC, p.id DESC
LIMIT 20 OFFSET 0;
```

Author filtered latest page:

```sql
EXPLAIN
SELECT p.*
FROM post p
WHERE p.author_id = 1
ORDER BY p.created_at DESC, p.id DESC
LIMIT 20 OFFSET 0;
```

Author and period filtered latest page:

```sql
EXPLAIN
SELECT p.*
FROM post p
WHERE p.author_id = 1
  AND p.created_at >= '2026-01-01 00:00:00'
  AND p.created_at <= '2026-01-31 23:59:59'
ORDER BY p.created_at DESC, p.id DESC
LIMIT 20 OFFSET 0;
```

Separated count query:

```sql
EXPLAIN
SELECT COUNT(p.id)
FROM post p
WHERE p.author_id = 1
  AND p.created_at >= '2026-01-01 00:00:00'
  AND p.created_at <= '2026-01-31 23:59:59';
```

Title keyword limitation:

```sql
EXPLAIN
SELECT p.*
FROM post p
WHERE p.title LIKE '%spring%'
ORDER BY p.created_at DESC, p.id DESC
LIMIT 20 OFFSET 0;
```

## Expected Reading Points

- Before indexes, filtered list queries commonly show full scan symptoms such as `type=ALL`, `key=NULL`, or `Using filesort`.
- After indexes, default sorting should prefer `idx_post_created_at_id`.
- After indexes, author-filtered sorting/range queries should prefer `idx_post_author_id_created_at_id`.
- `LIKE '%keyword%'` is intentionally not optimized by a B-tree index. Body/title full-text search is left for the next PR.
- Deep offset paging still scans/skips rows up to `OFFSET + LIMIT`. Cursor paging is left for an infinite-scroll style API.
- `PageableExecutionUtils` can skip the count query when the fetched content already proves the first page or last page size.
