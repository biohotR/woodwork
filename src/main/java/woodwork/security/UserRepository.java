package woodwork.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // used by Spring Security to find a user during login
    Optional<User> findByUsername(String username);

    // used during registration to prevent duplicate accounts
    Boolean existsByUsername(String username);

    // used during registration to prevent duplicate emails
    Boolean existsByEmail(String email);
}
