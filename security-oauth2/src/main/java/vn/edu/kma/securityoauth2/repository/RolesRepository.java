package vn.edu.kma.securityoauth2.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.kma.securityoauth2.entity.Roles;
import vn.edu.kma.securityoauth2.enums.RoleCode;

public interface RolesRepository extends JpaRepository<Roles, Long> {
    Optional<Roles> findByCode(RoleCode code);
}
