package vn.edu.kma.securityoauth2.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
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
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtDecoder jwtDecoder,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
            CustomAccessDeniedHandler customAccessDeniedHandler) {

        // SecurityContextHolderFilter
        //      ├── Load SecurityContext
        //      ├── SecurityContextHolder.set(SecurityContext)

        http
                // STATELESS: không lưu SecurityContext vào HTTP Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // CorsFilter
                .cors(Customizer.withDefaults())

                // CsrfFilter
                .csrf(AbstractHttpConfigurer::disable)

                // AuthentcationFilter
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder))
                        .authenticationEntryPoint(customAuthenticationEntryPoint))

                // ExceptionTranslatonFilter
                .exceptionHandling((ehc) -> ehc.authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))

                // AuthorizationFilter
                // Layer 1 - endpoints level
                .authorizeHttpRequests(request -> request

                        // Phân vùng chức năng: "Role này có được truy cập khu vực API này không?"
                        .requestMatchers("/api/v1/public/**")
                        .permitAll()
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/v1/management/**")
                        .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/v1/students/**")
                        .hasAnyRole("ADMIN", "MANAGER", "STUDENT")
                        .requestMatchers("/api/v1/lecturers/**")
                        .hasAnyRole("ADMIN", "MANAGER", "LECTURER")
                        .anyRequest()
                        .authenticated());

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
