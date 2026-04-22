package woodwork.product;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// starts the real spring context and database
@SpringBootTest
@ActiveProfiles("test")
class ProductConcurrencyIT {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private UUID testProductId;

    @BeforeEach
    void setUp() {
        Product product = new Product();
        product.setName("Test Hammer");
        product.setStockQuantity(50);
        Product savedProduct = productRepository.save(product);
        testProductId = savedProduct.getId();
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteById(testProductId);
    }

    @Test
    void deductStock_ShouldPreventRaceConditions_WhenMultipleThreadsExecuteSimultaneously() throws InterruptedException {
        // simulate 100 different users
        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        
        // used so the threads start at the same time
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    productService.deductStock(testProductId, 1);
                } catch (IllegalStateException e) {
                    // 50 threads will throw an exception because stock will run out.
                    // catch the errors so the test doens't crash
                } finally {
                    latch.countDown();
                }
            });
        }

        // wait for the threads to finish
        latch.await();

        // fetch final stock
        Product finalProduct = productRepository.findById(testProductId).orElseThrow();

        // should be 0
        assertEquals(0, finalProduct.getStockQuantity(), "Stock should never drop below zero during race conditions");
    }
}
