package woodwork.payment;

import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import woodwork.cart.Cart;
import woodwork.cart.CartItem;
import woodwork.cart.CartService;

@Service
public class PaymentService {

    private final CartService cartService;

    public PaymentService(CartService cartService) {
        this.cartService = cartService;
    }

    public PaymentIntentResponseDto createPaymentIntent(String username) throws StripeException {
        // get user's cart from the database
        Cart cart = cartService.getCartForUser(username);

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot create payment intent: The cart is empty.");
        }

        // calculate the total
        long totalCents = 0L;
        for (CartItem item : cart.getItems()) {
            totalCents += (long) item.getProduct().getPrice() * item.getQuantity();
        }

        // build stripe request
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(totalCents)
                .setCurrency("usd")
                .putMetadata("username", username)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        // send the request to stripe's servers and get the secret back
        PaymentIntent paymentIntent = PaymentIntent.create(params);

        return new PaymentIntentResponseDto(paymentIntent.getClientSecret());
    }
}
