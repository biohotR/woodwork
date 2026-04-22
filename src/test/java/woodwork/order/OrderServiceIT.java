package woodwork.order;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import woodwork.cart.Cart;
import woodwork.cart.CartItem;
import woodwork.cart.CartItemRepository;
import woodwork.cart.CartRepository;
import woodwork.product.Product;
import woodwork.product.ProductRepository;
import woodwork.security.User;
import woodwork.security.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceIT {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // We need the Cart repositories to set up the test state
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private User testUser;
    private Product productA;
    private Product productB;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("integration_tester");
        testUser.setPassword("hashedpassword123!");
        testUser.setEmail("integration@woodwork.com");
        userRepository.save(testUser);

        productA = new Product();
        productA.setName("Table Saw");
        productA.setPrice(50000);
        productA.setStockQuantity(10);
        productRepository.save(productA);

        productB = new Product();
        productB.setName("Wood Glue");
        productB.setPrice(500); 
        productB.setStockQuantity(1); 
        productRepository.save(productB);

        // Initialize an empty cart for the user
        testCart = new Cart();
        testCart.setUser(testUser);
        cartRepository.save(testCart);
    }

    @AfterEach
    void tearDown() {
        // Clean up in reverse order to respect foreign key constraints
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createPendingOrder_ShouldDeductStockSaveOrderAndClearCart_WhenSuccessful() {
        // 1. Arrange: Add 2 Table Saws to the database cart
        CartItem item1 = new CartItem();
        item1.setCart(testCart);
        item1.setProduct(productA);
        item1.setQuantity(2);
        cartItemRepository.save(item1);

        // 2. Act: Call the new method
        Order order = orderService.createPendingOrderFromCart(testUser.getUsername());

        // 3. Assert: Order state
        assertNotNull(order);
        assertEquals(OrderStatus.PENDING, order.getStatus(), "Order should be reserved as PENDING");
        assertEquals(100000, order.getTotalAmount(), "Total should be 100,000 cents");

        // 4. Assert: Stock was deducted
        Product updatedProduct = productRepository.findById(productA.getId()).orElseThrow();
        assertEquals(8, updatedProduct.getStockQuantity(), "Stock should be reduced from 10 to 8");

        // 5. Assert: Cart was successfully cleared
        Cart updatedCart = cartRepository.findByUserUsername(testUser.getUsername()).orElseThrow();
        assertTrue(updatedCart.getItems().isEmpty(), "Cart should be empty after successful order creation");
    }

    @Test
    void createPendingOrder_ShouldRollbackEverything_WhenOneItemFails() {
        // 1. Arrange: Add valid item and an out-of-stock item
        CartItem item1 = new CartItem();
        item1.setCart(testCart);
        item1.setProduct(productA);
        item1.setQuantity(1);
        cartItemRepository.save(item1);

        CartItem item2 = new CartItem();
        item2.setCart(testCart);
        item2.setProduct(productB);
        item2.setQuantity(5); // Only 1 in stock, this will trigger the crash
        cartItemRepository.save(item2);

        // 2. Act & Assert: Verify it throws the exception
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            orderService.createPendingOrderFromCart(testUser.getUsername());
        });
        assertTrue(exception.getMessage().contains("Out of stock"));

        // 3. Assert: Product A stock rolled back (didn't stay at 9)
        Product rolledBackProductA = productRepository.findById(productA.getId()).orElseThrow();
        assertEquals(10, rolledBackProductA.getStockQuantity(), "Stock for Product A should have rolled back to 10!");

        // 4. Assert: No order was saved
        List<Order> orders = orderRepository.findAll();
        assertTrue(orders.isEmpty(), "No order should have been saved to the database!");

        // 5. Assert: Cart was NOT cleared (because the transaction crashed before reaching the clear method)
        Cart rolledBackCart = cartRepository.findByUserUsername(testUser.getUsername()).orElseThrow();
        assertFalse(rolledBackCart.getItems().isEmpty(), "Cart should still contain items because the transaction failed!");
    }
}
