package woodwork.cart;

import java.security.Principal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
// if logged in => you have a cart
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart(Principal principal) {
        Cart cart = cartService.getCartForUser(principal.getName());
        return ResponseEntity.ok(CartDto.fromEntity(cart));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItemToCart(
            Principal principal,
            @Valid @RequestBody CartItemRequestDto request) {
        
        Cart cart = cartService.addItemToCart(principal.getName(), request);
        return ResponseEntity.ok(CartDto.fromEntity(cart));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartDto> removeItemFromCart(
            Principal principal,
            @PathVariable UUID productId) {
        
        Cart cart = cartService.removeItemFromCart(principal.getName(), productId);
        return ResponseEntity.ok(CartDto.fromEntity(cart));
    }

    @DeleteMapping
    public ResponseEntity<String> clearCart(Principal principal) {
        cartService.clearCart(principal.getName());
        return ResponseEntity.ok("Cart cleared successfully.");
    }

    @PostMapping("/sync")
    public ResponseEntity<CartDto> syncCart(
            Principal principal,
            @RequestBody woodwork.cart.CartSyncRequestDto request) {
        
        Cart cart = cartService.syncCart(principal.getName(), request.getItems());
        return ResponseEntity.ok(CartDto.fromEntity(cart));
    }
}
