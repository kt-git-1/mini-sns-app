package com.example.backend.config;

import com.example.backend.security.AuthUser;
import com.example.backend.web.log.AccessLogFilter;
import com.example.backend.web.log.RequestIdFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final RequestIdFilter requestIdFilter;
    private final AccessLogFilter accessLogFilter;

    public SecurityConfig(RequestIdFilter requestIdFilter, AccessLogFilter accessLogFilter) {
        this.requestIdFilter = requestIdFilter;
        this.accessLogFilter = accessLogFilter;
    }

    /** パスワードハッシュ用（signupで使用） */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** ローカル開発用CORS（Next.js 3000番から叩けるように） */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://127.0.0.1:3000"));
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /** HS256用 JwtDecoder */
    @Bean
    JwtDecoder jwtDecoder(@Value("${auth.jwt.secret}") String secret) {
        var key = new javax.crypto.spec.SecretKeySpec(
                secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
        return org.springframework.security.oauth2.jwt.NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256)
                .build();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health/**", "/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"unauthorized\"}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"forbidden\"}");
                        })
                )
                // Resource Server + JwtDecoder + カスタムConverter
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthUserConverter())
                        )
                );

        // 順序：リクエストID →（Bearerトークン処理）→ アクセスログ
        http.addFilterBefore(requestIdFilter, BearerTokenAuthenticationFilter.class);
        http.addFilterAfter(accessLogFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    /** JWTのクレーム → principal(AuthUser) + GrantedAuthority 変換 */
    @Bean
    Converter<Jwt, AbstractAuthenticationToken> jwtAuthUserConverter() {
        var rolesConv = new JwtGrantedAuthoritiesConverter();
        rolesConv.setAuthoritiesClaimName("roles"); // 発行側のclaim名に合わせる
        rolesConv.setAuthorityPrefix("ROLE_");

        return jwt -> {
            var authorities = rolesConv.convert(jwt);

            Long id = Optional.ofNullable((Number) jwt.getClaim("user_id"))
                    .map(Number::longValue)
                    .orElseThrow(() -> new IllegalArgumentException("user_id missing"));

            String username = Optional.ofNullable(jwt.getClaimAsString("username"))
                    .orElse(jwt.getSubject());

            Set<String> roles = Optional.ofNullable(jwt.getClaimAsStringList("roles"))
                    .map(Set::copyOf)
                    .orElseGet(Set::of);

            var principal = new AuthUser(id, username, roles);
            return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    principal, "N/A", authorities
            );
        };
    }
}