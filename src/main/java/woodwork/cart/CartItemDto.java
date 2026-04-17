package woodwork.cart;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemDto {
    private UUID productId;
    private String productName;
    private Integer quantity;
    private Integer unitPrice;
    private Long lineTotal;

    public static CartItemDto fromEntity(CartItem item) {
        CartItemDto dto = new CartItemDto();
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        
        int priceCents = item.getProduct().getPrice();
        dto.setUnitPrice(priceCents);
        
        // subtotal for row
        dto.setLineTotal((long) priceCents * item.getQuantity());
        
        return dto;
    }
}
