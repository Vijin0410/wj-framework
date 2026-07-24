package com.wangjin.common.security.config;

import cn.hutool.core.util.StrUtil;
import com.wangjin.common.constant.SecurityConstants;
import com.wangjin.common.security.context.LoginUser;
import com.wangjin.common.security.context.UserContext;
import com.wangjin.common.security.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 从 Authorization 头解析 JWT 并写入 SecurityContext / UserContext。
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader(jwtProperties.getHeader());
            if (StrUtil.isNotBlank(header)) {
                String token = header;
                if (header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
                    token = header.substring(SecurityConstants.TOKEN_PREFIX.length()).trim();
                }
                if (StrUtil.isNotBlank(token)) {
                    Claims claims = JwtUtils.parseToken(token, jwtProperties.getSecret());
                    if (!JwtUtils.isExpired(claims)) {
                        LoginUser loginUser = JwtUtils.toLoginUser(claims);
                        UserContext.set(loginUser);

                        List<SimpleGrantedAuthority> authorities = loginUser.getPermissions() == null
                                ? Collections.emptyList()
                                : loginUser.getPermissions().stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(loginUser, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}
