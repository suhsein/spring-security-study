package com.example.oauth2jwt.dto;

public interface OAuth2Response {
    // 제공자 (Ex. naver, google, ...)
    String getProvider();
    // 제공자에서 발급해주는 아이디(번호) 계정마다 고유한 값
    String getProviderId();
    // 이메일
    String getEmail();
    // 사용자의 계정 설정 이름
    String getName();
}

