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
    public String processCheckout(String username, CheckoutRequestDto checkoutRequest) {
        // identify the buyer
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // initialize order
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        
        Long runningTotal = 0L;

        // process each item in the cart
        for (OrderItemRequestDto itemRequest : checkoutRequest.getItems()) {
            
            // pessimistic lock on this specific product
            Product product = productRepository.findByIdWithLock(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemRequest.getProductId()));

            // check if enough stock
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new IllegalStateException("Not enough stock for: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());

            // line item for the cart
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPurchasePrice(product.getPrice());

            // add to order and update total
            order.getItems().add(orderItem);
            
            // Long lineItemTotal = BigDecimal.valueOf(product.getPrice())
            //         .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            Long lineItemTotal = (long) product.getPrice() * itemRequest.getQuantity();
            runningTotal += lineItemTotal;
        }

        order.setTotalAmount(runningTotal);

        // save the order (cascade logic will automatically save the OrderItems too)
        orderRepository.save(order);

        return "Checkout successful. Order ID: " + order.getId();
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
    public void markOrderAsPaidByUsername(String username) {
        // get the user's cart
        woodwork.cart.Cart cart = cartService.getCartForUser(username);

        if (cart.getItems().isEmpty()) {
            System.out.println("Webhook fired, but cart is empty for user: " + username);
            return;
        }

        User user = cart.getUser();
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PAID);

        long totalAmount = 0L;

        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findByIdWithLock(cartItem.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + cartItem.getProduct().getName()));

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException("Payment succeeded, but out of stock for: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            int unitPriceCents = product.getPrice();
            int quantity = cartItem.getQuantity();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setPurchasePrice(unitPriceCents);


            order.getItems().add(orderItem);
            totalAmount += ((long) unitPriceCents * quantity);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        cartService.clearCart(username);
        System.out.println("Payment verified! Order " + savedOrder.getId() + " created and inventory updated.");
    }
}
