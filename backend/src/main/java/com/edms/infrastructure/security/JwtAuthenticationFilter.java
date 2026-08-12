package com.edms.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.edms.infrastructure.persistence.entity.UserEntity;
import com.edms.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenValidator tokenValidator;
    private final UserJpaRepository userRepository;

    public JwtAuthenticationFilter(TokenValidator tokenValidator, UserJpaRepository userRepository) {
        this.tokenValidator = tokenValidator;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = getJwtFromRequest(request);

        if (StringUtils.hasText(token)) {
            tokenValidator.validate(token).ifPresent(claims -> {
                // Ưu tiên role từ DB (đúng phân quyền thật). Cognito groups có thể rỗng
                // nên role trong token không đáng tin bằng role trong bảng users.
                UserEntity user = userRepository.findByCognitoSub(claims.subject())
                        .or(() -> userRepository.findByEmail(claims.email()))
                        .orElse(null);

                String role = user != null ? user.getRole().name() : claims.role();
                String internalUserId = user != null ? user.getId() : claims.subject();

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        internalUserId,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
