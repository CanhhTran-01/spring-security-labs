package vn.edu.kma.securityoauth2.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private long expiresInMs;
}
