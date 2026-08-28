package vn.edu.kma.securityoauth2.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import vn.edu.kma.securityoauth2.exception.ErrorCode;
import vn.edu.kma.securityoauth2.response.ApiResponse;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        // quy định ErrorCode
        ErrorCode errorCode = ErrorCode.FORBIDDEN;

        // handle response trả về
        response.setStatus(errorCode.getHttpStatus().value()); // set Status cho response
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // kết quả trả về là JSON string
        response.setCharacterEncoding(StandardCharsets.UTF_8.name()); // chuẩn định dạng tiếng Việt

        // tạo content
        ApiResponse<Object> apiResponse = ApiResponse.error(errorCode, request.getRequestURI());

        // Lấy Writer của HTTP response, ghi JSON vào response body
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));

        // flush dữ liệu xuống client
        response.flushBuffer();
    }
}
