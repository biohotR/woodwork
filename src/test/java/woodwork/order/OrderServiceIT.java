package woodwork.order;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import woodwork.product.Product;
import woodwork.product.ProductRepository;
import woodwork.security.User;
import woodwork.security.UserRepository;

@SpringBootTest
class OrderServiceIT {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Product productA;
    private Product productB;

    @BeforeEach
    void setUp() {
        // create real user in the database
        testUser = new User();
        testUser.setUsername("integration_tester");
        testUser.setPassword("hashedpassword123!");
        testUser.setEmail("integration@woodwork.com");
        userRepository.save(testUser);

        // 2. create product a with stock
        productA = new Product();
        productA.setName("Table Saw");
        productA.setPrice(50000);
        productA.setStockQuantity(10);
        productRepository.save(productA);

        // create product b with less stock
        productB = new Product();
        productB.setName("Wood Glue");
        productB.setPrice(500); // 5.00 in cents
        productB.setStockQuantity(1); // Only 1 left in stock!
        productRepository.save(productB);
    }

    @AfterEach
    void tearDown() {
        // clean up
        orderRepository.deleteAll();
        productRepository.delete(productA);
        productRepository.delete(productB);
        userRepository.delete(testUser);
    }

    @Test
    void processCheckout_ShouldDeductStockAndSaveOrder_WhenSuccessful() {
        // buy 2 table saws
        OrderItemRequestDto item1 = new OrderItemRequestDto();
        item1.setProductId(productA.getId());
        item1.setQuantity(2);

        CheckoutRequestDto request = new CheckoutRequestDto();
        request.setItems(List.of(item1));

        String result = orderService.processCheckout(testUser.getUsername(), request);

        assertTrue(result.contains("Checkout successful"));

        // verify stock deducted properly
        Product updatedProduct = productRepository.findById(productA.getId()).orElseThrow();
        assertEquals(8, updatedProduct.getStockQuantity());

        // verify the receipt was saved
        List<Order> orders = orderRepository.findByUserId(testUser.getId());
        assertEquals(1, orders.size());
        assertEquals(100000, orders.get(0).getTotalAmount().doubleValue());
    }

    @Test
    void processCheckout_ShouldRollbackEverything_WhenOneItemFails() {
        OrderItemRequestDto item1 = new OrderItemRequestDto();
        item1.setProductId(productA.getId());
        item1.setQuantity(1);

        OrderItemRequestDto item2 = new OrderItemRequestDto();
        item2.setProductId(productB.getId());
        item2.setQuantity(5); // this will trigger the exception
        

        CheckoutRequestDto request = new CheckoutRequestDto();
        request.setItems(List.of(item1, item2));

        // verify it throws the exception
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            orderService.processCheckout(testUser.getUsername(), request);
        });

        assertTrue(exception.getMessage().contains("Not enough stock"));

        // did product a stock roll back?
        // if broken, should be 9
        // if not, it reverses the deduction and returns to 10.
        Product rolledBackProductA = productRepository.findById(productA.getId()).orElseThrow();
        assertEquals(10, rolledBackProductA.getStockQuantity(), "Stock for Product A should have rolled back to 10!");

        // check if accidentally save a partial receipt to the database
        List<Order> orders = orderRepository.findByUserId(testUser.getId());
        assertTrue(orders.isEmpty(), "No order should have been saved to the database!");
    }
}
