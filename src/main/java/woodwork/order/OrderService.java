package woodwork.order;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import woodwork.product.Product;
import woodwork.product.ProductRepository;
import woodwork.security.User;
import woodwork.security.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public String processCheckout(String username, CheckoutRequestDto checkoutRequest) {
        // 1. Identify the buyer
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2. Initialize the Order (Receipt)
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        
        BigDecimal runningTotal = BigDecimal.ZERO;

        // 3. Process each item in the cart
        for (OrderItemRequestDto itemRequest : checkoutRequest.getItems()) {
            
            // Acquire a pessimistic lock on this specific product
            Product product = productRepository.findByIdWithLock(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemRequest.getProductId()));

            // Verify stock
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new IllegalStateException("Not enough stock for: " + product.getName());
            }

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());

            // Build the line item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPurchasePrice(BigDecimal.valueOf(product.getPrice()));
            // Add to order and update total
            order.getItems().add(orderItem);
            
            BigDecimal lineItemTotal = BigDecimal.valueOf(product.getPrice())
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            runningTotal = runningTotal.add(lineItemTotal);
        }

        order.setTotalAmount(runningTotal);

        // 4. Save the order (Cascade logic will automatically save the OrderItems too)
        orderRepository.save(order);

        return "Checkout successful. Order ID: " + order.getId();
    }

    @Transactional
    public String updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        // 1. Find the order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        // 2. Prevent illegal state transitions
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update the status of a cancelled order.");
        }
        if (order.getStatus() == OrderStatus.SHIPPED && newStatus == OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot revert a shipped order back to pending.");
        }

        // 3. Update the status
        order.setStatus(newStatus);
        
        // 4. Save the updated order
        orderRepository.save(order);

        // Note: This is exactly where you would trigger your EmailService 
        // to send a "Your order has shipped!" notification in the future.

        return "Order status successfully updated to: " + newStatus;
    }
}
