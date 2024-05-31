package com.example.test_security.repository;

import com.example.test_security.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Boolean existsByUsername(String username);

    UserEntity findByUsername(String username);
}
