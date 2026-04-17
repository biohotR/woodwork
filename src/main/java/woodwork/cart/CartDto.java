package woodwork.cart;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartDto {
    private UUID id;
    private List<CartItemDto> items;
    private Long cartTotal;

    public static CartDto fromEntity(Cart cart) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        
        // convert entities to dtos
        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(CartItemDto::fromEntity)
                .collect(Collectors.toList());
        dto.setItems(itemDtos);

        // total price calculation
        Long total = itemDtos.stream()
                .mapToLong(CartItemDto::getLineTotal)
                .sum();

        dto.setCartTotal(total);

        return dto;
    }
}
