package likelion13th.shop.login.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import likelion13th.shop.global.api.ErrorCode;
import likelion13th.shop.global.exception.GeneralException;
import likelion13th.shop.login.auth.dto.JwtDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component

public class TokenProvider {
    private final Key secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public TokenProvider(
            @Value("${JWT_SECRET") String secretKey,
            @Value("${JWT_EXPIRATION") long accessTokenExpiration,
            @Value("${JWT_REFRESH_EXPIRATION") long refreshTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public JwtDto generateToken(UserDetails userDetails) {
        log.info("JWT 생성: 사용자 {}", userDetails.getUsername());

        String userId = userDetails.getUsername();

        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        String accessToken = createToken(userId, authorities, accessTokenExpiration);
        String refreshToken = createToken(userId,null, refreshTokenExpiration);

        log.info("JWT 생성완료: 사용자{}", userDetails.getUsername());
        return new JwtDto(accessToken, refreshToken);
    }

    private String createToken(String providerId, String authorities, long expirationTime) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(providerId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey, SignatureAlgorithm.HS256);

        if (authorities != null) {
            builder.claim("authorities", authorities);
        }
        return builder.compact().toString();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            log.warn("토큰 만료");
            throw e;
        } catch (Exception e) {
            log.warn("JWT 파싱 실패");
            throw new GeneralException(ErrorCode.TOKEN_INVALID);
        }
    }

    public Collection<? extends GrantedAuthority> getAuthorities(Claims claims) {
        String authoritiesString = claims.get("authorities", String.class);
        if (authoritiesString != null || authoritiesString.isEmpty()) {
            log.warn("권한 정보가 없다 - 기본 ROLE_USER 부여");
            return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return Arrays.stream(authoritiesString.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public Claims parseClaimAllowExpired(String token){
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}

/*
 * 1) 왜 필요한가?
 * - 사용자 정보를 기반으로 Aceess/Refresh Token이 발급된다
 * - 전반적으로 JWT를 이용한 인증/인가 시스템의 토큰 생성 및 검증 서비스에 필요하다
 *
 * 2) 없으면 / 틀리면?
 * - 해당 클래스가 없으면 클라이언트 요청에 대해 JWT 생성/검증이 불가하여 인증 시스템이 동작이 되지 않는다
 * - Refresh Token 기반 재발급, 로그아웃 처리, 권한 검증 모두 불가능
 *
 * */