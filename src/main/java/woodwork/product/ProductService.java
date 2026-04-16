package woodwork.product;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import woodwork.category.Category;
import woodwork.category.CategoryRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    // add both repositories for the service to work
    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          FileStorageService fileStorageService) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.fileStorageService = fileStorageService;
    }

    public ProductResponse getAllProducts(int pageNo, int pageSize, String sortBy, String sortDir,
                                            String searchTerm, Double minPrice, Double maxPrice, UUID categoryId
    ) {
        
        // determine the sorting direction
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // pagination instructions
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        // generate the sql rules based on what the user typed
        Specification<Product> spec = ProductSpecification.filterProducts(searchTerm, minPrice, maxPrice, categoryId);

        // ask the database for the specific piece of data
        Page<Product> products = productRepository.findAll(spec, pageable);

        // convert the slice of entities into a slice of dtos
        List<ProductDto> content = products.getContent().stream()
                .map(ProductDto::fromEntity)
                .toList();

        // package it all into a Product Response
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(content);
        productResponse.setPageNo(products.getNumber());
        productResponse.setPageSize(products.getSize());
        productResponse.setTotalElements(products.getTotalElements());
        productResponse.setTotalPages(products.getTotalPages());
        productResponse.setLast(products.isLast());

        return productResponse;
    }

    @Transactional
    public void deductStock(UUID productId, int requestedQuantity) {
        // find product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // check if enough stock
        if (product.getStockQuantity() < requestedQuantity) {
            throw new IllegalStateException("Not enough stock available for product: " + product.getName());
        }

        // deduct the requested quantity
        product.setStockQuantity(product.getStockQuantity() - requestedQuantity);

        // save updated product
        productRepository.save(product);
    }

    // create a blank entity and copy the data over to it
    public ProductDto createProduct(ProductDto dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());

        // relational database part
        // if a category is found, we link the product and category together
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + dto.getCategoryId()));
            product.setCategory(category);
        }

        // save and return
        Product savedProduct = productRepository.save(product);
        return ProductDto.fromEntity(savedProduct);
    }

    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }
        productRepository.deleteById(id);
    }

    public ProductDto uploadProductImage(UUID productId, org.springframework.web.multipart.MultipartFile file) {
        // check if the product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        // save the physical file to the disk
        String imageUrl = fileStorageService.saveFile(file);

        // update the database record with the new url
        product.setImageUrl(imageUrl);
        Product savedProduct = productRepository.save(product);

        // Return the updated dto
        return ProductDto.fromEntity(savedProduct);
    }
}
