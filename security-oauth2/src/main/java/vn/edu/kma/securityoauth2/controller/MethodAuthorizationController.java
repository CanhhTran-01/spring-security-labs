package vn.edu.kma.securityoauth2.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.kma.securityoauth2.response.ApiResponse;

@Slf4j
@RestController
@RequestMapping("/api/v1/students/")
@RequiredArgsConstructor
public class MethodAuthorizationController {

    /**
     * Method Level: Phân quyền hành động.
     * Login với User có role STUDENT để check.
     * "/api/v1/students/**" không có nghĩa là STUDENT được làm mọi thứ.
     * Student vượt qua "Layer 1 - endpoints level", nhưng có thể bị "Layer 2 - method level" chặn tại đây.
     **/
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STUDENT')")
    @GetMapping("/case-1")
    public ResponseEntity<ApiResponse<String>> case1() {
        return ResponseEntity.ok(ApiResponse.successWithMessage("Bạn CÓ thể truy vào đây!"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/case-2")
    public ResponseEntity<ApiResponse<String>> case2() {
        return ResponseEntity.ok(ApiResponse.successWithMessage("Bạn KHÔNG thể truy vào đây được!"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/case-3")
    public ResponseEntity<ApiResponse<String>> case3() {
        return ResponseEntity.ok(ApiResponse.successWithMessage("Bạn KHÔNG thể truy vào đây được!"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/case-4")
    public ResponseEntity<ApiResponse<String>> case4() {
        return ResponseEntity.ok(ApiResponse.successWithMessage("Bạn KHÔNG thể truy vào đây được!"));
    }
}
