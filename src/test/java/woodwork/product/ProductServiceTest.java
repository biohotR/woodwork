package woodwork.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    // We have to mock both dependencies that the ProductService relies on
    @Mock
    private ProductRepository productRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ProductService productService;

    @Test
    void uploadProductImage_WhenProductDoesNotExist_ShouldThrowException() {
        UUID fakeProductId = UUID.randomUUID();
        //  mock standard Spring objects like MultipartFile
        MultipartFile mockFile = mock(MultipartFile.class);

        // explicitly tell the fake database to return nothing
        when(productRepository.findById(fakeProductId)).thenReturn(Optional.empty());

        // if it doesn't crash, the test fails.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.uploadProductImage(fakeProductId, mockFile);
        });

        // Verify the error message is exactly what we wrote in our Service
        assertEquals("Product not found with ID: " + fakeProductId, exception.getMessage());

        // verify that the FileStorageService was never triggered.
        // don't want to save a physical file to the hard drive if the product doesn't exist
        verifyNoInteractions(fileStorageService);
    }
}
