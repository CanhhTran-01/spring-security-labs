package vn.edu.kma.securityoauth2.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtDecoder jwtDecoder) {
        http
                .csrf(AbstractHttpConfigurer::disable) // tắt csrf
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // stateless vì dùng JWT
                // phân quyền tầng 1 - Endpoint Layer
                .authorizeHttpRequests(request -> request
                        // ai cùng vào được - .permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        // phải đăng nhập để vào  - .anyRequest().authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")   // Phải là ADMIN
                        .requestMatchers("/api/lecturer/**").hasRole("LECTURER")   // Phải là LECTURER
                        .requestMatchers("/api/student/**").hasRole("STUDENT")   // Phải là STUDENT
                        .anyRequest().authenticated()
                )
                // config Resource Server dùng bean JWT Decoder phục vụ xác minh JWT trong request's header
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder)));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_"); // để match hasRole()

        // đọc đúng Custom-claim tên "role" trong JWT (default là "scope" hoặc "scp")
        grantedAuthoritiesConverter.setAuthoritiesClaimName("role");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }
}

