package com.example.springbestpractice.infrastructure.user;

import com.example.springbestpractice.domain.user.User;
import com.example.springbestpractice.domain.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {

    Page<User> search(String keyword, UserRole role, Pageable pageable);
}
