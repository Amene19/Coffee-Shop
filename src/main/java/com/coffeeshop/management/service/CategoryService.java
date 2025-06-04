package com.coffeeshop.management.service;

import com.coffeeshop.management.model.Category;
import com.coffeeshop.management.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }
    
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }
    
    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    @Transactional
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Category name already exists: " + category.getName());
        }
        
        return categoryRepository.save(category);
    }
    
    @Transactional
    public Category updateCategory(Category category) {
        Category existingCategory = categoryRepository.findById(category.getId())
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + category.getId()));
        
        // Check if the new name is already taken by another category
        if (!existingCategory.getName().equals(category.getName()) && 
            categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Category name already exists: " + category.getName());
        }
        
        return categoryRepository.save(category);
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}