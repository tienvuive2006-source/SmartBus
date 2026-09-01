package com.smartbus.booking.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Tắt CSRF vì dùng JWT (stateless)
            .csrf(AbstractHttpConfigurer::disable)
            // Cấu hình CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Không dùng session (JWT là stateless)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Phân quyền các endpoint
            .authorizeHttpRequests(auth -> auth
                // ✅ Public endpoints - không cần token
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/ai/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/health/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/trips/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/routes/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/route-bus-types/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/route-vehicles/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/route-stops/routes/**", "/route-stops/trips/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/bus-types/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/buses/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/seats/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/reviews/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/articles/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/settings/**").permitAll()
                // Cho phép khách vãng lai đặt vé và kiểm tra thanh toán
                .requestMatchers("/admin/bookings/**").permitAll()
                .requestMatchers("/seat-holds/**").permitAll()
                // Webhook từ các đối tác thứ 3 (SePay, Momo...)
                .requestMatchers("/webhook/**").permitAll()
                // Cho phép kết nối WebSocket
                .requestMatchers("/ws/**").permitAll()
                // ⚡ H2 console (chỉ dùng khi dev)
                .requestMatchers("/h2-console/**").permitAll()
                // 🔒 Admin endpoints - chỉ ADMIN
                .requestMatchers("/dashboard/**").hasRole("ADMIN")
                .requestMatchers("/saved-locations/**").hasRole("ADMIN")
                .requestMatchers("/vehicle-maintenance/**").hasRole("ADMIN")
                .requestMatchers("/refund-requests/admin", "/refund-requests/admin/**").hasRole("ADMIN")
                .requestMatchers("/ticket-exchanges/admin").hasRole("ADMIN")
                .requestMatchers("/route-stops/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/route-stops/driver/**").hasAnyRole("ADMIN", "DRIVER")
                .requestMatchers(HttpMethod.PUT, "/settings/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/routes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/routes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/routes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/route-bus-types/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/route-vehicles/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/route-drivers/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/route-inspectors/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/route-stops/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/bus-types/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/bus-types/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/bus-types/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/trips/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/trips/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/trips/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/buses/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/buses/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/buses/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/users/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/users/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")
                // 🎫 Inspector & Driver endpoints
                .requestMatchers("/inspector/**").hasAnyRole("ADMIN", "INSPECTOR", "DRIVER")
                // 🔐 Các route còn lại cần đăng nhập
                .anyRequest().authenticated()
            )
            // Thêm JWT filter TRƯỚC UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ✅ BCrypt để mã hoá mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ CORS config - cho phép Vue frontend gọi API
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
