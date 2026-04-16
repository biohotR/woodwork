package woodwork.product;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductDto {
    private UUID id;

    @NotBlank(message = "Product name cannot be empty")
    private String name;

    private String description; // optional

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be strictly greater than zero")
    private Double price;
    
    @NotNull(message = "Category ID is required to create a product")
    private UUID categoryId;
    
    private String categoryName;

    private String imageUrl;
    private Integer stockQuantity;

    public static ProductDto fromEntity(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        return dto;
    }
}
