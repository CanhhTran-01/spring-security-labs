package vn.edu.kma.securityoauth2.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.edu.kma.securityoauth2.entity.User;

import java.util.Collection;
import java.util.Collections;

/**
 * Wrapper CustomUserDetails bọc entity User để Spring Security dùng làm principal.

 * Vì sao dùng wrapper này thay vì cho entity User implements UserDetails luôn ?
 * ĐỂ: giữ entity User thuần JPA, không trộn hợp đồng UserDetails vào
 * ĐỂ: tránh lộ password/authorities khi trả JSON, tránh rối với quan hệ LAZY roles.

 * Nếu cho User implements UserDetails trực tiếp: ít code hơn, không cần
 * getUser(), nhưng entity bị gắn chặt vào Spring Security. Chỉ hợp demo nhỏ.
 */

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleCode = user.getRoles().getCode().toString();
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + roleCode));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();   // định danh đăng nhập là email
    }
}
