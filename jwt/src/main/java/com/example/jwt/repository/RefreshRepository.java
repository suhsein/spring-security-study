package com.example.jwt.repository;

import com.example.jwt.entity.RefreshEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {
    Boolean existsByRefresh(@Param("refresh") String refresh);
    @Transactional
    void deleteByRefresh(String refresh);
}
