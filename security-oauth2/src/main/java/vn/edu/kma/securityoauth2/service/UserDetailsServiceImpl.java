package vn.edu.kma.securityoauth2.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.kma.securityoauth2.entity.User;
import vn.edu.kma.securityoauth2.repository.UserRepository;
import vn.edu.kma.securityoauth2.security.CustomUserDetails;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true) // role là LAZY, CustomUserDetails đọc role -> cần session đang mở
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {

        // ném UsernameNotFoundException thay vì AppException để DaoAuthenticationProvider
        // đổi thành BadCredentialsException, không để lộ lý do xác thực sai
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Chỉ load user, KHÔNG so mật khẩu hay tiêu chí khác ở đây, Spring sẽ lo việc này

        return new CustomUserDetails(user);
    }
}

//      return lại UserDetails cho DaoAuthenticationProvider (Spring)
//      DaoAuthenticationProvider sẽ tự làm tiếp:
//            ├─ nhận CustomUserDetails từ loadUserByUsername
//            ├─ kiểm tra isEnabled / isAccountNonLocked ...
//            ├─ passwordEncoder.matches(raw, userDetails.getPassword())
//            └─ đúng thì tạo Authentication mới đã xác thực:
//                          - principal   = CustomUserDetails   // chính object vừa return
//                          - credentials = null
//                          - authorities = userDetails.getAuthorities()