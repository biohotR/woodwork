package woodwork.order;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusUpdateRequestDto {

    @NotNull(message = "New status cannot be null")
    private OrderStatus newStatus;
}
