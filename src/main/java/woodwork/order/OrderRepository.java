package woodwork.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    // This allows you to easily fetch a user's order history later
    List<Order> findByUserId(UUID userId);
    List<Order> findByUserUsername(String username);
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime date);
}
