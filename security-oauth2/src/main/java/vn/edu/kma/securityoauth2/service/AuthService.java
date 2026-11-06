package vn.edu.kma.securityoauth2.service;

import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import vn.edu.kma.securityoauth2.response.UserInfoResponse;
import vn.edu.kma.securityoauth2.security.CustomUserDetails;
import vn.edu.kma.securityoauth2.security.JwtProvider;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional // tuỳ nghiệp vụ, ở đây sẽ là gửi cả role
    public void register(RegisterRequest request, RoleCode roleCode) {

        // check email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        // check role, nếu chưa có thì tự tạo (cân nhắc sau)
        Roles roles = rolesRepository
                .findByCode(roleCode)
                .orElseGet(() -> rolesRepository.save(Roles.builder().code(roleCode).build()));

        // tạo mật khẩu ngẫu nhiên
        String rawPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 10);

        // save user (account) với mật khẩu ngẫu nhiên vừa tạo
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .roles(roles)
                .build();

        // ghi xuống DB
        user = userRepository.save(user);
        log.info("Password for testing: {}", rawPassword);

        // tạo event
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(user.getId())
                .roleCode(user.getRoles().getCode())
                .email(user.getEmail())
                .build();

        // publish event sang cho module khác lắng nghe
        eventPublisher.publishEvent(event);
        log.info(
                "Published UserRegisteredEvent for userId: {} (email: {}, role: {})",
                user.getId(),
                user.getEmail(),
                user.getRoles().getCode());
    }

    @Transactional(readOnly = true)
    public AuthResponse login1(LoginRequest request) {

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

    public AuthResponse login2(LoginRequest request){

        // 1. Xác thực. Mọi lỗi (sai mật khẩu, không có user, khóa...) là AuthenticationException
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        }
//        catch (LockedException e) {
//            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
//        } catch (DisabledException e) {
//            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        catch (AuthenticationException e) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 2. BỎ SecurityContextHolder.setAuthentication(...)
        // API stateless, request sau sẽ xác thực lại bằng JWT
        // context này sống hết request login rồi bị xóa, không có tác dụng.

        // 3. Lấy user từ principal
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = Objects.requireNonNull(userDetails).getUser();

        // TODO: có thể tạo thêm trường trả về của các đối tượng cụ thể, tùy vào role của họ: STUDENT, LECTURER, ...
        UserInfoResponse userInfoResponse = UserInfoResponse.builder()
                // tạo userInfoResponse...
                .build();

        // 4. Sinh token
        String token = jwtProvider.generateToken(currentUser);

        return AuthResponse.builder()
                .userInfoResponse(userInfoResponse)
                .accessToken(token)
                .build();
    }
}
