package woodwork.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    
    // custom query to find a role by its exact name (example: "ROLE_ADMIN")
    Optional<Role> findByName(String name);
}
