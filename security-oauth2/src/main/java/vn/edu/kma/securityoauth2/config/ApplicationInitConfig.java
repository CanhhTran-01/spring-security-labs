package vn.edu.kma.securityoauth2.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.kma.securityoauth2.entity.Roles;
import vn.edu.kma.securityoauth2.entity.User;
import vn.edu.kma.securityoauth2.enums.RoleCode;
import vn.edu.kma.securityoauth2.repository.RolesRepository;
import vn.edu.kma.securityoauth2.repository.UserRepository;

@Configuration
@Slf4j
public class ApplicationInitConfig {

    @Value("${app.init.admin.email}")
    private String adminEmail;

    @Value("${app.init.admin.password:12345}")
    private String adminPassword;

    @Bean
    ApplicationRunner applicationRunner(
            UserRepository userRepository, RolesRepository rolesRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail(adminEmail).isEmpty()) {

                // Kiểm tra, không tạo mới nếu Role ADMIN đã có
                Roles roles = rolesRepository
                        .findByCode(RoleCode.ADMIN)
                        .orElseGet(() -> rolesRepository.save(
                                Roles.builder().code(RoleCode.ADMIN).build()));

                User user = User.builder()
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .roles(roles)
                        .build();

                userRepository.save(user);

                log.info("Default Admin user has been created with password: {}", adminPassword);
            }
        };
    }
}
