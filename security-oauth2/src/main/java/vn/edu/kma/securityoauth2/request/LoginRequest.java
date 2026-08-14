package vn.edu.kma.securityoauth2.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
