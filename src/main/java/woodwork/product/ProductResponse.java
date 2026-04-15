package woodwork.product;

import java.util.List;

import lombok.Data;

@Data
public class ProductResponse {
    private List<ProductDto> content;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
