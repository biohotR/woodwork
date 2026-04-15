package woodwork.category;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryDto::fromEntity)
                .toList();
    }

    public CategoryDto createCategory(CategoryDto dto) {
        // convert dto to entity
        Category category = new Category();
        category.setName(dto.getName());
        
        // save to the database
        Category savedEntity = categoryRepository.save(category);
        
        // convert to dto and return
        return CategoryDto.fromEntity(savedEntity);
    }

    public void deleteCategory(UUID id) {
        if (!categoryRepository.existsById(id)) {
            // this triggers your global exception handler
            throw new IllegalArgumentException("Category not found with ID: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
