package com.example.test_security.service;

import com.example.test_security.dto.JoinDto;
import com.example.test_security.entity.UserEntity;
import com.example.test_security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public void joinProcess(JoinDto joinDto) {

        // 중복 회원명 조회 로직
        if(userRepository.existsByUsername(joinDto.getUsername())){
            return;
        }

        UserEntity userEntity = new UserEntity();

        userEntity.setUsername(joinDto.getUsername());
        // 비밀번호 DB 저장 시 bcrypt encoder 로 인코딩 후 저장
        userEntity.setPassword(bCryptPasswordEncoder.encode(joinDto.getPassword()));
        userEntity.setRole("ROLE_ADMIN");

        userRepository.save(userEntity);
    }
}
