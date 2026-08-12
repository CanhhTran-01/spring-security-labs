package vn.edu.kma.securityoauth2.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import vn.edu.kma.securityoauth2.entity.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtEncoder jwtEncoder;

    @Value("${jwt.valid-duration}")
    private Long VALID_DURATION;

    public String generateToken(User user) {
        Instant now = Instant.now(); // tính expired time cho JWT

        // 1. Cấu hình Header (Thuật toán HS512)
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS512).build();

        // 2. Cấu hình Payload (Claims)
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("security-oauth2")
                .issuedAt(now)
                .expiresAt(now.plus(VALID_DURATION, ChronoUnit.SECONDS))
                .subject(user.getId().toString())
                .claim("role", user.getRoles().getCode()) // Custom-claim
                .build();

        // 3. Ký và nhả ra Token String
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
