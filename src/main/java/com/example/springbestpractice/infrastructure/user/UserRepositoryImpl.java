package com.example.springbestpractice.infrastructure.user;

import com.example.springbestpractice.domain.user.QUser;
import com.example.springbestpractice.domain.user.User;
import com.example.springbestpractice.domain.user.UserRole;
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

import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

    private static final QUser user = QUser.user;

    private final JPAQueryFactory queryFactory;

    public UserRepositoryImpl(EntityManager entityManager, ObjectProvider<JPAQueryFactory> queryFactoryProvider) {
        this.queryFactory = queryFactoryProvider.getIfAvailable(() -> new JPAQueryFactory(entityManager));
    }

    @Override
    public Page<User> search(String keyword, UserRole role, Pageable pageable) {
        List<User> content = queryFactory.selectFrom(user)
                .where(
                        keywordContains(keyword),
                        roleEq(role)
                )
                .orderBy(orderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(user.count())
                .from(user)
                .where(
                        keywordContains(keyword),
                        roleEq(role)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return user.email.containsIgnoreCase(keyword)
                .or(user.nickname.containsIgnoreCase(keyword));
    }

    private BooleanExpression roleEq(UserRole role) {
        if (role == null) {
            return null;
        }
        return user.role.eq(role);
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
            case "createdAt" -> new OrderSpecifier<>(direction, user.createdAt);
            case "id" -> new OrderSpecifier<>(direction, user.id);
            default -> throw new IllegalArgumentException("Unsupported user sort property: " + order.getProperty());
        };
    }
}
