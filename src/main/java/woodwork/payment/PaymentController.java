package woodwork.payment;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import woodwork.order.Order;
import woodwork.order.OrderService;

@RestController
@RequestMapping("/api/payment")
@PreAuthorize("hasRole('CUSTOMER')")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    public PaymentController(PaymentService paymentService, OrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntentResponseDto> createIntent(Principal principal) {
        try {
            // 1. Create the Pending Order and deduct stock
            Order order = orderService.createPendingOrderFromCart(principal.getName());
            
            // 2. Give that Order to Stripe
            PaymentIntentResponseDto response = paymentService.createPaymentIntent(order);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize payment gateway: " + e.getMessage());
        }
    }
}
