package com.example.springbestpractice.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static User create(String email, String nickname, String password) {
        return create(email, nickname, password, UserRole.USER);
    }

    public static User createAdmin(String email, String nickname, String password) {
        return create(email, nickname, password, UserRole.ADMIN);
    }

    private static User create(String email, String nickname, String password, UserRole role) {
        return User.builder()
                .email(requireNotBlank(email, "이메일은 필수입니다."))
                .nickname(requireNotBlank(nickname, "닉네임은 필수입니다."))
                .password(requireNotBlank(password, "비밀번호는 필수입니다."))
                .role(requireRole(role))
                .build();
    }

    public void updateNickname(String nickname) {
        this.nickname = requireNotBlank(nickname, "닉네임은 필수입니다.");
    }

    public void updatePassword(String password) {
        this.password = requireNotBlank(password, "비밀번호는 필수입니다.");
    }

    private static String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static UserRole requireRole(UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("User role is required.");
        }
        return role;
    }
}
