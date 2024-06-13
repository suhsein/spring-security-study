package com.example.oauth2jwt.service;

import com.example.oauth2jwt.dto.*;
import com.example.oauth2jwt.entity.UserEntity;
import com.example.oauth2jwt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // default 구현체로 user 정보의 loading 을 위임함. (액세스 토큰을 사용해 리소스 서버에서 유저정보 획득)
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("oAuth2User = " + oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = null;

        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();

        // DB 조회 후, 데이터 저장 혹은 업데이트
        UserEntity existData = userRepository.findByUsername(username);
        String role = "ROLE_USER";

        if (existData == null) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(username);
            userEntity.setName(oAuth2Response.getName());
            userEntity.setEmail(oAuth2Response.getEmail());
            userEntity.setRole(role);

            userRepository.save(userEntity);
        } else {
            existData.setName(oAuth2Response.getName());
            existData.setEmail(oAuth2Response.getEmail());
            role = existData.getRole();
        }

        UserDto userDto = new UserDto();
        userDto.setUsername(username);
        userDto.setName(oAuth2Response.getName());
        userDto.setRole(role);

        return new CustomOAuth2User(userDto);
    }
}
