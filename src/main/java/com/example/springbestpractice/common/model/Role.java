package com.example.springbestpractice.common.model;

/**
 * 인증 주체(LoginUser)의 권한을 표현하는 common 소유 값.
 * 도메인 계층의 {@code UserRole}과 값은 동일하되, common이 domain에 의존하지 않도록
 * 별도로 정의한다. 매핑은 도메인에 접근 가능한 계층(예: CustomUserDetails)에서 수행한다.
 */
public enum Role {
    USER,
    ADMIN
}
