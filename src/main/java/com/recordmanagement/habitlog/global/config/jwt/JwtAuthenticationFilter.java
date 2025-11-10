package com.recordmanagement.habitlog.global.config.jwt;

import com.recordmanagement.habitlog.domain.auth.domain.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JwtAuthenticationFilter - JWT 인증 필터
 *
 * 모든 HTTP 요청에 대해 실행되며, Authorization 헤더에서 JWT 토큰을 추출한다.
 * 토큰이 유효한 경우 SecurityContext에 인증 정보를 설정하여
 * Spring Security가 인증된 사용자로 인식하도록 한다.
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 요청마다 호출되는 필터 메서드
     *
     * @param request  HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 필터 체인 객체
     * @throws ServletException, IOException 필터 처리 중 예외 발생 시 던져짐
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    // 블랙리스트 확인
                    if (tokenBlacklistService.isTokenBlacklisted(token)) {
                        log.debug("블랙리스트된 토큰 접근 시도");
                        SecurityContextHolder.clearContext();
                    } else {
                        String userId = jwtTokenProvider.getUserIdAsStringFromToken(token);

                        // 인증 객체 생성 (권한 정보는 빈 리스트로 처리)
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

                        // SecurityContext에 인증 정보 저장
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("JWT 토큰 인증 성공 - userId={}", userId);
                    }
                } else {
                    // 토큰이 유효하지 않음 (만료 등) - 401 응답
                    log.debug("유효하지 않은 JWT 토큰 - 401 응답");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"토큰이 만료되었거나 유효하지 않습니다.\"}");
                    return;
                }
            } catch (Exception e) {
                log.error("JWT 토큰 인증 중 오류 발생", e);
                SecurityContextHolder.clearContext();
                // 토큰 파싱 오류 시에도 401 응답
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"토큰이 만료되었거나 유효하지 않습니다.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     *
     * @param request HTTP 요청
     * @return JWT 토큰 문자열, 없으면 null 반환
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}