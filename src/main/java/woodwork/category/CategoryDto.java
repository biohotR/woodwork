package woodwork.category;

import java.util.UUID;

import lombok.Data;

@Data
public class CategoryDto {
    private UUID id;
    private String name;

    public static CategoryDto fromEntity(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}
