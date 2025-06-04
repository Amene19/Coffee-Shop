package com.coffeeshop.management.repository;

import com.coffeeshop.management.model.Category;
import com.coffeeshop.management.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByAvailableTrue();
    
    Page<Product> findByCategoryAndAvailableTrue(Category category, Pageable pageable);
    
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchProducts(String keyword, Pageable pageable);
    
    @Query("SELECT p, COUNT(oi) AS orderCount " +
           "FROM Product p LEFT JOIN OrderItem oi ON p.id = oi.product.id " +
           "GROUP BY p ORDER BY orderCount DESC")
    List<Product> findBestSellingProducts(Pageable pageable);
}