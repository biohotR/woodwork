package woodwork.product;

import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ProductResponse getAllProducts(
                @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
                @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
                @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
                @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir
    ) {
        return productService.getAllProducts(pageNo, pageSize, sortBy, sortDir);
    }

    @PostMapping
    public ProductDto createProduct(@Valid @RequestBody ProductDto productDto) {
        return productService.createProduct(productDto);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
    }
}
