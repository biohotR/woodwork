package woodwork.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import jakarta.annotation.PostConstruct;
import woodwork.cart.CartService;

@Service
public class PaymentService {

    private final CartService cartService;
    
    @Value("${stripe.api.key}")
    private String stripeSecretKey;

    public PaymentService(CartService cartService) {
        this.cartService = cartService;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public PaymentIntentResponseDto createPaymentIntent(woodwork.order.Order order) throws StripeException {
        
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(order.getTotalAmount())
                .setCurrency("usd")
                .putMetadata("orderId", order.getId().toString()) 
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        return new PaymentIntentResponseDto(paymentIntent.getClientSecret());
    }
}
