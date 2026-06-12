package com.example.springbestpractice.support.fixture;

import com.example.springbestpractice.domain.user.User;
import org.springframework.test.util.ReflectionTestUtils;

public final class UserFixture {

    private UserFixture() {
    }

    public static User user() {
        return User.create("test@test.com", "tester", "password");
    }

    public static User userWithId(Long id) {
        User user = user();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    public static User userWithId(Long id, String email, String nickname, String password) {
        User user = User.create(email, nickname, password);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    public static User adminWithId(Long id) {
        User user = User.createAdmin("admin@test.com", "admin", "password");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
