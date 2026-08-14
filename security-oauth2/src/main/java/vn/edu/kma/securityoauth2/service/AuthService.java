package vn.edu.kma.securityoauth2.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.kma.securityoauth2.config.security.JwtProvider;
import vn.edu.kma.securityoauth2.entity.Roles;
import vn.edu.kma.securityoauth2.entity.User;
import vn.edu.kma.securityoauth2.enums.RoleCode;
import vn.edu.kma.securityoauth2.event.UserRegisteredEvent;
import vn.edu.kma.securityoauth2.exception.AppException;
import vn.edu.kma.securityoauth2.exception.ErrorCode;
import vn.edu.kma.securityoauth2.repository.RolesRepository;
import vn.edu.kma.securityoauth2.repository.UserRepository;
import vn.edu.kma.securityoauth2.request.LoginRequest;
import vn.edu.kma.securityoauth2.request.RegisterRequest;
import vn.edu.kma.securityoauth2.response.AuthResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // enum USER_NOT_FOUND cho cả 2 case: không tồn tại & sai mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        String token = jwtProvider.generateToken(user);

        return AuthResponse.builder().accessToken(token).build();
    }

    @Transactional // tuỳ nghiệp vụ, ở đây sẽ là gửi cả role
    public void register(RegisterRequest request, RoleCode roleCode) {

        // check email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        // check role, nếu chưa có thì tự tạo (cân nhắc sau)
        Roles roles = rolesRepository
                .findByCode(roleCode)
                .orElseGet(() ->
                        rolesRepository.save(Roles.builder().code(roleCode).build()));

        // save user (account)
        String rawPassword = generateDefaultPwd();
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .roles(roles)
                .build();
        user = userRepository.save(user); // giữ lại user vừa save()
        log.info("Password for testing: {}", rawPassword);

        // publish event sang cho module khác lắng nghe
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(user.getId())
                .roleCode(user.getRoles().getCode())
                .email(user.getEmail())
                .build();

        eventPublisher.publishEvent(event);
        log.info(
                "Published UserRegisteredEvent for userId: {} (email: {}, role: {})",
                user.getId(),
                user.getEmail(),
                user.getRoles().getCode());
    }

    private String generateDefaultPwd() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }
}
