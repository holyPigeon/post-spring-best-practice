package com.example.springbestpractice.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private User(String email, String nickname, String password) {
        this.email = requireNotBlank(email, "이메일은 필수입니다.");
        this.nickname = requireNotBlank(nickname, "닉네임은 필수입니다.");
        this.password = requireNotBlank(password, "비밀번호는 필수입니다.");
    }

    public static User create(String email, String nickname, String password) {
        return new User(email, nickname, password);
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
}
