package com.example.jwt.service;

import com.example.jwt.entity.UserEntity;
import com.example.jwt.dto.CustomUserDetails;
import com.example.jwt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // DB 에서 조회
        UserEntity userData = userRepository.findByUsername(username);

        if (userData != null) {
            // UserDetails 에 담아서 return
            return new CustomUserDetails(userData);
        }
        return null;
    }
}
