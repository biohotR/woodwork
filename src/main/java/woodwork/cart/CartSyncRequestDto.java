package woodwork.cart;

import java.util.List;

public class CartSyncRequestDto {
    private List<CartItemRequestDto> items;

    public List<CartItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemRequestDto> items) {
        this.items = items;
    }
}
