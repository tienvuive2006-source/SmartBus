package com.smartbus.booking.config;

import com.smartbus.booking.repository.UserRepository;
import com.smartbus.booking.entity.User;
import com.smartbus.booking.config.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Không có token hoặc không đúng format "Bearer ..." -> bỏ qua
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String token = authHeader.substring(7); // Cắt "Bearer "
            final Long userId = jwtService.extractUserId(token);

            // Nếu chưa được xác thực trong session hiện tại
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Kiểm tra user tồn tại trong DB
                java.util.Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    if (user.getIsLocked() != null && user.getIsLocked()) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"error\": \"ACCOUNT_LOCKED\", \"message\": \"Tài khoản của bạn đã bị khóa!\"}");
                        return; // Chặn request đi tiếp
                    }
                    if (!jwtService.isTokenExpired(token)) {
                        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + user.getRole())
                        );

                        // Sử dụng userId làm principal (chuỗi String)
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                String.valueOf(user.getId()), null, authorities
                        );

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        } catch (Exception e) {
            // Token lỗi -> không set authentication -> Spring Security sẽ xử lý
            log.warn("JWT validation failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
