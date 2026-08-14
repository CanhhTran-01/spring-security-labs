package vn.edu.kma.securityoauth2.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.kma.securityoauth2.enums.RoleCode;
import vn.edu.kma.securityoauth2.request.LoginRequest;
import vn.edu.kma.securityoauth2.request.RegisterRequest;
import vn.edu.kma.securityoauth2.response.ApiResponse;
import vn.edu.kma.securityoauth2.response.AuthResponse;
import vn.edu.kma.securityoauth2.service.AuthService;

@Slf4j
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @RequestBody RegisterRequest request, @RequestParam String code) {
        authService.register(request, RoleCode.valueOf(code));
        return ResponseEntity.ok(
                ApiResponse.successWithMessage("Tạo tài khoản thành công, đăng nhập để dùng dịch vụ!"));
    }
}
