package com.coffeeshop.management.service;

import com.coffeeshop.management.model.Category;
import com.coffeeshop.management.model.Product;
import com.coffeeshop.management.repository.CategoryRepository;
import com.coffeeshop.management.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }
    
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }
    
    public List<Product> findAvailableProducts() {
        return productRepository.findByAvailableTrue();
    }
    
    public Page<Product> findProductsByCategory(Long categoryId, Pageable pageable) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
        
        return productRepository.findByCategoryAndAvailableTrue(category, pageable);
    }
    
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable);
    }
    
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }
    
    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
    
    @Transactional
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    
    @Transactional
    public void changeProductAvailability(Long id, boolean available) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setAvailable(available);
        productRepository.save(product);
    }
    
    public List<Product> findBestSellingProducts(int limit) {
        return productRepository.findBestSellingProducts(Pageable.ofSize(limit));
    }
}