package vn.edu.kma.securityoauth2.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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

@Configuration
public class ApplicationConfig {

    @Value("${jwt.secret}")
    private String signerKey;

    /**
     * Bean dùng để TẠO JWT Token, hiện tại chỉ dùng mặc định, không custom
     **/
    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKey key = new SecretKeySpec(signerKey.getBytes(), "HmacSHA256");
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    /**
     * Bean dùng để XÁC THỰC JWT Token, hiện tại chỉ dùng mặc định, không custom
     **/
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(signerKey.getBytes(), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * Bean dùng để mã hóa mật khẩu trước khi lưu vào DB - thuật toán BCrypt
     **/
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
