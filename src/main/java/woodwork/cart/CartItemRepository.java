package woodwork.cart;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") UUID cartId);
}
