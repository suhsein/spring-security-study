package com.example.oauth2jwt.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;


public class CustomOAuth2User implements OAuth2User {
    private final UserDto userDto;

    public CustomOAuth2User(UserDto userDto) {
        this.userDto = userDto;
    }

    /**
     * 외부 서비스별로 가지고 있는 유저정보 형태가 다르기 때문에,
     * attributes 를 제공하는 메서드는 구현하지 않는다.
     * 이런 이유 때문에 모든 attributes 를 주입받지 않았고,
     * DTO 를 사용해서 꼭 필요한 정보만을 주입받았음.
     * (DTO 를 사용하는 이유는 Entity 를 사용하는 경우 무결성 관련 위험. 역할 분리 필요하기 때문)
     */
    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userDto.getRole();
            }
        });
        return collection;
    }

    @Override
    public String getName() {
        return userDto.getName();
    }

    public String getUsername() {
        return userDto.getUsername();
    }
}
