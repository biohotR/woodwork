package woodwork.cart;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    
    //  allows us to find a cart using the user's username
    Optional<Cart> findByUserUsername(String username);
}
