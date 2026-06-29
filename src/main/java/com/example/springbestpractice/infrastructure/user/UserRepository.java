package com.example.springbestpractice.infrastructure.user;

import com.example.springbestpractice.domain.user.User;
import com.example.springbestpractice.domain.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    long countByRole(UserRole role);
}
