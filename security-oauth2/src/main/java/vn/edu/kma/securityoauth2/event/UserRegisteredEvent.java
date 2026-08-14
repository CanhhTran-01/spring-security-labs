package vn.edu.kma.securityoauth2.event;

import lombok.Builder;
import lombok.Data;
import vn.edu.kma.securityoauth2.enums.RoleCode;

@Data
@Builder
public class UserRegisteredEvent {
    private final Long userId;
    private final String email;
    private final RoleCode roleCode;
}
