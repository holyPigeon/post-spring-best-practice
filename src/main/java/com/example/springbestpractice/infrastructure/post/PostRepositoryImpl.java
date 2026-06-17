package com.example.springbestpractice.infrastructure.post;

import com.example.springbestpractice.domain.post.Post;
import com.example.springbestpractice.domain.post.QPost;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PostRepositoryImpl implements PostRepositoryCustom {

    private static final QPost post = QPost.post;

    private final JPAQueryFactory queryFactory;

    public PostRepositoryImpl(EntityManager entityManager, ObjectProvider<JPAQueryFactory> queryFactoryProvider) {
        this.queryFactory = queryFactoryProvider.getIfAvailable(() -> new JPAQueryFactory(entityManager));
    }

    @Override
    public Page<Post> search(String keyword, Long authorId, LocalDateTime createdFrom, LocalDateTime createdTo, Pageable pageable) {
        List<Post> content = queryFactory.selectFrom(post)
                .where(
                        titleContains(keyword),
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
                        authorIdEq(authorId),
                        createdAtGoe(createdFrom),
                        createdAtLoe(createdTo)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression titleContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return post.title.contains(keyword);
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
