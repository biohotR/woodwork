package woodwork.order;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequestDto {

    @NotEmpty(message = "Cart cannot be empty")
    @Valid
    private List<OrderItemRequestDto> items;
}
