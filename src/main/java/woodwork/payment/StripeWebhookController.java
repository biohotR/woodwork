package woodwork.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;

import woodwork.order.OrderService;

@RestController
@RequestMapping("/api/webhook/stripe")
public class StripeWebhookController {

    private final OrderService orderService;

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    public StripeWebhookController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<String> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            // Cryptographically verify this actually came from Stripe
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            System.out.println("Webhook signature verification failed.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        // We only care about successful payments right now
        if ("payment_intent.succeeded".equals(event.getType())) {
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            
            if (dataObjectDeserializer.getObject().isPresent()) {
                PaymentIntent paymentIntent = (PaymentIntent) dataObjectDeserializer.getObject().get();
                
                // Read the nametag we attached in the PaymentService
                String username = paymentIntent.getMetadata().get("username");

                if (username != null) {
                    System.out.println("Payment succeeded for user: " + username);
                    orderService.markOrderAsPaidByUsername(username);
                }
            }
        }

        // Always return 200 OK fast so Stripe doesn't think the server crashed
        return ResponseEntity.ok("Success");
    }
}
