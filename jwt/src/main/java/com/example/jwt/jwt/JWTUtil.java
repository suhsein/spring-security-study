package com.example.jwt.jwt;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT
 * 크게 세 부분. 헤더, 페이로드, 서명
 *
 * 1. Header
 * JWT 임을 명시. 사용된 암호화 알고리즘 정보를 담음
 * 2. Payload
 * 정보. 외부에서 열람해도 되는 정보만 담아야 함.
 * 3. Signature(서명)
 *  { BASE64 방식으로 인코딩 된 헤더 + BASE64 방식으로 인코딩 된 페이로드 + 암호화 키(key 객체)} 를 암호화 알고리즘으로 암호화 한 결과.
 * 서명을 확인하여 토큰의 무결성을 검증한다. 헤더와 페이로드를 변경하지 않았는지 확인.
 *
 *
 * 페이로드에 저장되는 정보 (Claim 이라고 한다.)
 *
 * 1. username
 * 2. role
 * 3. 생성일
 * 4. 만료일
 */
@Component
public class JWTUtil {
    private final SecretKey secretKey;

    /**
     * String 을 기반으로 SecretKey 라는 객체 키를 생성함.
     * String key 는 JWT 에서 사용되지 않기 때문이다.
     *
     * 이 프로젝트에서는 서명 검증을 위해서 HS256 알고리즘(양방향 대칭키 알고리즘) 사용
     */
    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    /**
     * Jwts 는 JWT 의 생성과, JWT 의 파싱 및 검증을 위한 static 메서드들을 제공하는 팩토리 클래스이다.
     * 이를 사용해서 JWT 생성 메서드와, 검증 메서드들을 작성한다.
     */
    public String getUsername(String token){
        /**
         * JWS(Json Web Signature) : JWT 의 일종으로, JWT 에 서명된 데이터를 포함하는 표준이다.
         *                  JWS 는 JWT 의 페이로드와 함께 서명된 토큰을 생성하고 전달할 때 사용된다.
         *
         * Jwts.parser()
         *      .verifyWith(secretKey)
         *      .build();
         * : JWT 파서 생성
         *
         * JWT 파서 빌더의 verifyWith() : JWT 의 서명을 검증하기 위해 사용됨.
         *      서명 검증을 위해 서버에서 처음 서명을 만들 때 sign 을 위해 사용한 secret Key 객체를 사용한다.
         *
         * parseSignedClaims() : 서명된 JWT 를 파싱하고, 내용을 확인함.
         *      서명이 유효한 경우에만 토큰 내용을 반환한다.
         */
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    public String getRole(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public String getCategory(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }

    public Boolean isExpired(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }


    public String createJwt(String category, String username, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("category", category)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }


}
