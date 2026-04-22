package woodwork.order;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import woodwork.cart.CartItem;
import woodwork.cart.CartService;
import woodwork.product.Product;
import woodwork.product.ProductRepository;
import woodwork.security.User;
import woodwork.security.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository,
                        CartService cartService
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    @Transactional
    public String updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        // find order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        // prevent illegal state transitions
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update the status of a cancelled order.");
        }
        if (order.getStatus() == OrderStatus.SHIPPED && newStatus == OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot revert a shipped order back to pending.");
        }

        // update status
        order.setStatus(newStatus);
        
        // save the updated order
        orderRepository.save(order);

        // MAYBE: to add an email notification
        return "Order status successfully updated to: " + newStatus;
    }

    @Transactional
    public Order createPendingOrderFromCart(String username) {
        woodwork.cart.Cart cart = cartService.getCartForUser(username);

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty cart.");
        }

        User user = cart.getUser();
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING); // Reserving it!

        long totalAmount = 0L;

        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findByIdWithLock(cartItem.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            // 1. Check stock
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException("Out of stock for: " + product.getName());
            }

            // 2. Deduct stock (Reserve it)
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPurchasePrice(product.getPrice());

            order.getItems().add(orderItem);
            totalAmount += ((long) product.getPrice() * cartItem.getQuantity());
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // 3. Clear the cart NOW, not later.
        cartService.clearCart(username); 
        
        return savedOrder;
    }
}
