package woodwork.order;

import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public String checkoutFromCart(String username) {
        // get user's active cart
        woodwork.cart.Cart cart = cartService.getCartForUser(username);

        // prevent empty checkout
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot process checkout: The cart is empty.");
        }

        User user = cart.getUser();

        // initialize receipt
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        
        // double totalAmount = 0.0;
        long totalAmount = 0L;

        // process inventory
        for (woodwork.cart.CartItem cartItem : cart.getItems()) {
            // lock the row in the database so nobody else can buy it simultaneously
            Product product = productRepository.findByIdWithLock(cartItem.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + cartItem.getProduct().getName()));

            // check stock
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException("Checkout failed: Not enough stock for " + product.getName() + 
                                                ". Only " + product.getStockQuantity() + " left.");
            }

            // deduct
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            int unitPriceCents = product.getPrice();
            int quantity = cartItem.getQuantity();
            long lineTotal = (long) unitPriceCents * quantity;

            // write the line item on the receipt
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPurchasePrice(unitPriceCents);

            order.getItems().add(orderItem);
            totalAmount += lineTotal;
        }

        order.setTotalAmount(totalAmount);

        // commit order to the database
        Order savedOrder = orderRepository.save(order);

        // wipe the cart clean
        cartService.clearCart(username);

        // email for order confirmation - add later

        return "Checkout successful from Cart! Order ID: " + savedOrder.getId();
    }

    @Async
    @Transactional
    public void markOrderAsPaidByUsername(String username) {
        // find the user's most recent PENDING order
        Order pendingOrder = orderRepository.findByUserUsername(username).stream()
                .filter(o -> o.getStatus().equals(OrderStatus.PENDING))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No pending order found for user: " + username));

        // flip the status
        pendingOrder.setStatus(OrderStatus.PAID);
        orderRepository.save(pendingOrder);

        // order confirmation - LATER
    }
}
