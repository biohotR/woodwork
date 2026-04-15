package woodwork.category;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    // @Mock creates a fake, empty version of the database repository
    @Mock
    private CategoryRepository categoryRepository;

    // @InjectMocks injects the fake repository into the CategoryService
    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_ShouldReturnCategoryDto() {
        // set up fake inputs and fake database response
        CategoryDto inputDto = new CategoryDto();
        inputDto.setName("Lumber");

        Category fakeSavedCategory = new Category();
        fakeSavedCategory.setId(UUID.randomUUID());
        fakeSavedCategory.setName("Lumber");

        // If the service tries to save ANYTHING, return this fake category
        when(categoryRepository.save(any(Category.class))).thenReturn(fakeSavedCategory);

        // call the actual method we want to test
        CategoryDto result = categoryService.createCategory(inputDto);

        // verify the logic worked exactly as expected
        assertNotNull(result.getId(), "The generated ID should not be null");
        assertEquals("Lumber", result.getName(), "The category name should match our input");
    }
}
