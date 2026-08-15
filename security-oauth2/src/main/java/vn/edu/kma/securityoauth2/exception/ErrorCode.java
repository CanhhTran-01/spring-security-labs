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
    UNAUTHENTICATED(4000, "Đăng nhập để sử dụng dịch vụ", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(4001, "Không có quyền truy cập", HttpStatus.FORBIDDEN),

    // enum cho cả 2 case: không tồn tại & sai mật khẩu
    USER_NOT_FOUND(4002, "Tài khoản hoặc mật khẩu không chính xác", HttpStatus.UNAUTHORIZED),

    EMAIL_EXISTED(4003, "Email đã tồn tại", HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND(4004, "Sai role code hoặc role không tương ứng không tồn tại", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
