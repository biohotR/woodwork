package woodwork.order;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import woodwork.product.Product;
import woodwork.product.ProductRepository;

@Service
public class OrderCleanupService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderCleanupService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    // Runs automatically every 15 minutes
    @Scheduled(fixedRate = 900000) 
    @Transactional
    public void restockAbandonedOrders() {
        
        // 1. Find orders that have been PENDING for more than 30 minutes
        LocalDateTime thirtyMinsAgo = LocalDateTime.now().minusMinutes(30);
        List<Order> abandonedOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, thirtyMinsAgo);

        for (Order order : abandonedOrders) {
            // 2. Add the items back to the shelf
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }

            // 3. Mark the order as CANCELLED so we don't process it again
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            
            System.out.println("Restocked abandoned order: " + order.getId());
        }
    }
}
