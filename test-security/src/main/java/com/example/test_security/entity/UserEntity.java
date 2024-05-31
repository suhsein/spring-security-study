package com.example.test_security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 중복 회원명 비허용
    @Column(unique = true)
    private String username;
    private String password;

    private String role;

    public UserEntity() {
    }
}
