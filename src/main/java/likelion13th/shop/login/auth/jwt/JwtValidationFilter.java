package likelion13th.shop.login.auth.jwt;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import likelion13th.shop.global.api.ApiResponse;
import likelion13th.shop.global.api.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.ServerException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        return "/users/reissue".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication existing =  SecurityContextHolder.getContext().getAuthentication();
        if(existing != null && existing.isAuthenticated() && !(existing instanceof AnonymousAuthenticationToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try{
            Claims claims = tokenProvider.parseClaims(token);

            String providerId = claims.getSubject();
            if (providerId == null || providerId.isEmpty()) {
             return;
            }

            var authorities = tokenProvider.getAuthFromClaims(claims);

            CustomUserDetails userDetails = new CustomUserDetails(
                    providerId,
                    "",
                    authorities
            );
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedURLException e){
            //잘못된 서명
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
        } catch (UnsupportedJwtException e){
            //토큰만료
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
        } catch (IllegalArgumentException e) {
            //널 공백 등 잘못된 입력
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
        } catch (Exception e) {
            //예기치 못한 오류
            sendErrorResponse(response, ErrorCode.TOKEN_INVALID);
        }
    }

    //표준 에러 응답 -> json반환
    //상태코드 401 통일
    //INVALD - 401, 기타 = 500 에러있다 -> 따로
    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(
                new ObjectMapper().writeValueAsString(ApiResponse.onFailure(errorCode, null))
        );
    }
}
