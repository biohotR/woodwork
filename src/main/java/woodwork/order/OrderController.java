package woodwork.order;

import java.security.Principal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> checkout(@Valid @RequestBody CheckoutRequestDto request, Principal principal) {
        String result = orderService.processCheckout(principal.getName(), request);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<String> updateStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderStatusUpdateRequestDto request) {
        
        String result = orderService.updateOrderStatus(orderId, request.getNewStatus());
        return ResponseEntity.ok(result);
    }
}
