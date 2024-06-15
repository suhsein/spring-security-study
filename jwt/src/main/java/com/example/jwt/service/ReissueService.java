package com.example.jwt.service;

import com.example.jwt.entity.RefreshEntity;
import com.example.jwt.jwt.JWTUtil;
import com.example.jwt.repository.RefreshRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class ReissueService {
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public ResponseEntity<?> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        // get refresh token
        String refresh = null;
        Cookie[] cookies = request.getCookies();

        refresh = Arrays.stream(cookies).filter((cookie) -> cookie.getName().equals("refresh"))
                .findFirst().get().getValue();

        if(refresh == null) {
            // response status code
            return new ResponseEntity("refresh token null", HttpStatus.BAD_REQUEST);
        }

        // expired check
        try {
            jwtUtil.isExpired(refresh);
        } catch(ExpiredJwtException e){
            // response status code
            return new ResponseEntity("access token expired", HttpStatus.BAD_REQUEST);
        }

        // 토큰이 refresh 인지 확인 (발급 시 페이로드에 명시)
        if (!jwtUtil.getCategory(refresh).equals("refresh")) {
            // response status code
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        // DB 에 저장되어 있는지 확인
        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        if(!isExist){
            // response status code
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // create new JWT(access token)
        String newAccess = jwtUtil.createJwt("access", username, role, 60 * 10 * 1000L);
        /**
         * Refresh Rotate -> 리프레시 토큰을 사용하여 액세스 토큰의 재발급 요청 시에, 리프레시 토큰 또한 재발급.
         * * 주의사항
         *   : 이전에 사용하던 리프레시 토큰의 만료가 되지 않은 경우에, 그 토큰을 가지고 액세스 토큰을 발급받는 문제
         *     만료 시간은 수정할 수 없다. 고로, 서버에서 리프레시 토큰을 관리하는 것이 필요함. => 블랙리스트 사용.
         *
         *  CSR 에서의 Refresh Rotate 절차
         *  1. 프론트엔드에서는 액세스 토큰 재발급 요청을 서버에 보냄 (리프레시 토큰 포함)
         *  2. 서버는 기존 리프레시 토큰을 검증하고 블랙리스트에 올림
         *  3. 서버는 새로운 액세스 토큰과 리프레시 토큰을 생성하여 프론트엔드로 반환
         *  4. 프론트엔드는 기존 리프레시 토큰을 쿠키 등 저장소에서 삭제
         *  5. 프론트엔드는 새로운 액세스 토큰과 리프레시 토큰을 적절한 저장소에 저장

         *  비슷하게 로그아웃 시에도 반드시 백엔드 측에서는 블랙리스트에 리프레시 토큰을 올리고,
         *  프론트엔드 측에서는 액세스 토큰, 리프레시 토큰을 저장소에서 삭제해야 한다.
         *
         *  * 블랙리스트 : 블랙리스트에 추가된 리프레시 토큰으로 온 요청에 대해서는 토큰 발급을 못하도록 함.
         *    블랙리스트 내부의 토큰들도 주기적으로 배치 or 스케줄링 통해 삭제 필요함(기간 만료된 경우).
         *    레디스 사용의 경우 TTL 로 만료된 토큰 삭제 가능.
         *
         *  혹은 반대로 사용 가능한 리프레시 토큰들을 저장해두고, 로테이트시 삭제하는 방식으로도 구현 가능.
         *  (내 생각에는 블랙리스트 쪽이 더 좋은 것 같다.
         *  자원 관리 측면에서 효율적이고 주기적인 관리를 할 때도 배치나 스케줄링으로 효율적으로 관리할 수 있기 때문이다.
         *  동기화 문제도 서버에 저장하는 것이 아닌, DB or 레디스에 저장하는 방식으로 해결할 수 있다.)
         */
        Long expiredMs = 60 * 60 * 24 * 1000L;
        String newRefresh = jwtUtil.createJwt("refresh", username, role, expiredMs);

        refreshRepository.deleteByRefresh(refresh);
        addRefreshEntity(username, newRefresh, expiredMs);

        /**
         * 모든 계정에서 로그아웃 시 username 을 사용하여 모든 액세스 토큰 삭제 가능.
         */

        // response
        response.setHeader("access", newAccess);
        response.addCookie(createCookie("refresh", refresh));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void addRefreshEntity(String username, String refresh, Long expiredMs){
        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }
    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
//        cookie.setSecure(true);
//        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }
}
