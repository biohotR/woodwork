package woodwork.payment;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/api/payment")
@PreAuthorize("hasRole('CUSTOMER')")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntentResponseDto> createIntent(Principal principal) {
        try {
            PaymentIntentResponseDto response = paymentService.createPaymentIntent(principal.getName());
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to initialize payment gateway: " + e.getMessage());
        }
    }
}
