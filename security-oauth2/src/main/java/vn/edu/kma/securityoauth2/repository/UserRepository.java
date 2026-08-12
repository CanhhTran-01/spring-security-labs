package vn.edu.kma.securityoauth2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.kma.securityoauth2.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
