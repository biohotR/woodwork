package woodwork.product;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

public class ProductSpecification {

    public static Specification<Product> filterProducts(String searchTerm, Double minPrice, Double maxPrice, UUID categoryId) {
        return (root, query, cb) -> {
            // this list will holds the dynamic WHERE clauses
            List<Predicate> predicates = new ArrayList<>();

            // search by name or description (case insensitive partial match)
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String likePattern = "%" + searchTerm.toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), likePattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), likePattern);
                predicates.add(cb.or(nameMatch, descMatch)); // name OR description
            }

            // filter by minimum price
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            // filter by maximum price
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // filter by category
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            // combine all the predicates with AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
