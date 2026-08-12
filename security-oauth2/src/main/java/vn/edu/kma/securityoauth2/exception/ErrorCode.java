package vn.edu.kma.securityoauth2.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // =====================================
    // 1xxx - General Errors
    // =====================================
    UNEXPECTED_ERROR(1000, "Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR(1001, "Dữ liệu đầu vào không hợp lệ", HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER(1002, "Thiếu tham số bắt buộc", HttpStatus.BAD_REQUEST),

    // =====================================
    // 3xxx - External Service Errors
    // =====================================

    // =====================================
    // 4xxx - Authentication & Authorization Errors
    // =====================================
    UNAUTHENTICATED(4000, "Vui lòng đăng nhập", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(4001, "Không có quyền truy cập", HttpStatus.FORBIDDEN);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}

