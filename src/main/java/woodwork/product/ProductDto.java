package woodwork.product;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductDto {
    
    private UUID productId;

    @NotBlank(message = "Product name cannot be empty")
    private String productName;

    private String description; 

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be strictly greater than zero")
    private Integer unitPrice;
    
    @NotNull(message = "Category ID is required to create a product")
    private UUID categoryId;
    
    private String categoryName;
    private String imageUrl;
    private Integer stockQuantity;

    public static ProductDto fromEntity(Product product) {
        ProductDto dto = new ProductDto();
        
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setUnitPrice(product.getPrice());
        
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        dto.setStockQuantity(product.getStockQuantity());
        
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        return dto;
    }
}
