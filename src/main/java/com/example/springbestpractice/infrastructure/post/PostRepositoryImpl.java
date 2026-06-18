package com.example.springbestpractice.infrastructure.post;

import com.example.springbestpractice.domain.post.Post;
import com.example.springbestpractice.domain.post.QPost;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class PostRepositoryImpl implements PostRepositoryCustom {

    private static final QPost post = QPost.post;

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;
    private final boolean fullTextSearchEnabled;

    public PostRepositoryImpl(
            EntityManager entityManager,
            ObjectProvider<JPAQueryFactory> queryFactoryProvider,
            @Value("${app.post.search.fulltext.enabled:true}") boolean fullTextSearchEnabled
    ) {
        this.entityManager = entityManager;
        this.queryFactory = queryFactoryProvider.getIfAvailable(() -> new JPAQueryFactory(entityManager));
        this.fullTextSearchEnabled = fullTextSearchEnabled;
    }

    @Override
    public Page<Post> search(
            String keyword,
            String contentKeyword,
            Long authorId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Pageable pageable
    ) {
        if (StringUtils.hasText(contentKeyword) && fullTextSearchEnabled) {
            return searchWithFullText(keyword, contentKeyword, authorId, createdFrom, createdTo, pageable);
        }
        return searchWithQueryDsl(keyword, contentKeyword, authorId, createdFrom, createdTo, pageable);
    }

    private Page<Post> searchWithQueryDsl(
            String keyword,
            String contentKeyword,
            Long authorId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Pageable pageable
    ) {
        List<Post> content = queryFactory.selectFrom(post)
                .where(
                        titleContains(keyword),
                        contentContains(contentKeyword),
                        authorIdEq(authorId),
                        createdAtGoe(createdFrom),
                        createdAtLoe(createdTo)
                )
                .orderBy(orderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(post.count())
                .from(post)
                .where(
                        titleContains(keyword),
                        contentContains(contentKeyword),
                        authorIdEq(authorId),
                        createdAtGoe(createdFrom),
                        createdAtLoe(createdTo)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @SuppressWarnings("unchecked")
    private Page<Post> searchWithFullText(
            String keyword,
            String contentKeyword,
            Long authorId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Pageable pageable
    ) {
        String fromWhere = fullTextFromWhere(keyword, authorId, createdFrom, createdTo);
        Query contentQuery = entityManager.createNativeQuery(
                "select p.*" + fromWhere + orderClause(pageable),
                Post.class
        );
        setFullTextParameters(contentQuery, keyword, contentKeyword, authorId, createdFrom, createdTo);
        contentQuery.setFirstResult(Math.toIntExact(pageable.getOffset()));
        contentQuery.setMaxResults(pageable.getPageSize());

        List<Post> content = contentQuery.getResultList();

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> countFullText(fromWhere, keyword, contentKeyword, authorId, createdFrom, createdTo)
        );
    }

    private BooleanExpression titleContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return post.title.contains(keyword);
    }

    private BooleanExpression contentContains(String contentKeyword) {
        if (!StringUtils.hasText(contentKeyword)) {
            return null;
        }
        return post.content.contains(contentKeyword);
    }

    private BooleanExpression authorIdEq(Long authorId) {
        if (authorId == null) {
            return null;
        }
        return post.authorId.eq(authorId);
    }

    private BooleanExpression createdAtGoe(LocalDateTime createdFrom) {
        if (createdFrom == null) {
            return null;
        }
        return post.createdAt.goe(createdFrom);
    }

    private BooleanExpression createdAtLoe(LocalDateTime createdTo) {
        if (createdTo == null) {
            return null;
        }
        return post.createdAt.loe(createdTo);
    }

    private OrderSpecifier<?>[] orderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = pageable.getSort().stream()
                .map(this::toOrderSpecifier)
                .toList();
        return orders.toArray(OrderSpecifier[]::new);
    }

    private String fullTextFromWhere(
            String keyword,
            Long authorId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo
    ) {
        StringBuilder sql = new StringBuilder(
                " from post p where match(p.content) against (:contentKeyword in natural language mode) > 0"
        );
        if (StringUtils.hasText(keyword)) {
            sql.append(" and p.title like :keyword");
        }
        if (authorId != null) {
            sql.append(" and p.author_id = :authorId");
        }
        if (createdFrom != null) {
            sql.append(" and p.created_at >= :createdFrom");
        }
        if (createdTo != null) {
            sql.append(" and p.created_at <= :createdTo");
        }
        return sql.toString();
    }

    private void setFullTextParameters(
            Query query,
            String keyword,
            String contentKeyword,
            Long authorId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo
    ) {
        query.setParameter("contentKeyword", contentKeyword);
        if (StringUtils.hasText(keyword)) {
            query.setParameter("keyword", "%" + keyword + "%");
        }
        if (authorId != null) {
            query.setParameter("authorId", authorId);
        }
        if (createdFrom != null) {
            query.setParameter("createdFrom", createdFrom);
        }
        if (createdTo != null) {
            query.setParameter("createdTo", createdTo);
        }
    }

    private long countFullText(
            String fromWhere,
            String keyword,
            String contentKeyword,
            Long authorId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo
    ) {
        Query countQuery = entityManager.createNativeQuery("select count(*)" + fromWhere);
        setFullTextParameters(countQuery, keyword, contentKeyword, authorId, createdFrom, createdTo);
        return ((Number) countQuery.getSingleResult()).longValue();
    }

    private String orderClause(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return "";
        }

        return pageable.getSort().stream()
                .map(this::toSqlOrder)
                .collect(Collectors.joining(", ", " order by ", ""));
    }

    private String toSqlOrder(Sort.Order order) {
        String direction = order.isAscending() ? "asc" : "desc";

        return switch (order.getProperty()) {
            case "createdAt" -> "p.created_at " + direction;
            case "id" -> "p.id " + direction;
            default -> throw new IllegalArgumentException("Unsupported post sort property: " + order.getProperty());
        };
    }

    private OrderSpecifier<?> toOrderSpecifier(Sort.Order order) {
        com.querydsl.core.types.Order direction = order.isAscending()
                ? com.querydsl.core.types.Order.ASC
                : com.querydsl.core.types.Order.DESC;

        return switch (order.getProperty()) {
            case "createdAt" -> new OrderSpecifier<>(direction, post.createdAt);
            case "id" -> new OrderSpecifier<>(direction, post.id);
            default -> throw new IllegalArgumentException("Unsupported post sort property: " + order.getProperty());
        };
    }
}
