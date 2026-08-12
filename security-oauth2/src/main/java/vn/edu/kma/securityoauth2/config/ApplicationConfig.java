package vn.edu.kma.securityoauth2.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
public class ApplicationConfig {

    @Value("${jwt.secret}")
    private String signerKey;

    /**
     Bean dùng để TẠO JWT Token
     **/
    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKey key = new SecretKeySpec(signerKey.getBytes(), "HmacSHA512");
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    /**
     Bean dùng để XÁC THỰC JWT Token
     **/
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(signerKey.getBytes(), "HmacSHA512");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS512).build();
    }

    /**
     Bean dùng để mã hóa mật khẩu trước khi lưu vào DB - thuật toán BCrypt
     **/
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
