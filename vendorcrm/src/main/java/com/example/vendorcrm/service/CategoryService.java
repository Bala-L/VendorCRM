package com.example.vendorcrm.service;


import com.example.vendorcrm.entity.Category;
import com.example.vendorcrm.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Create category
    public Category saveCategory(Category category) {

        if (categoryRepository.existsByCategoryName(
                category.getCategoryName())) {

            throw new RuntimeException("Category already exists");
        }

        return categoryRepository.save(category);
    }

    // Get all categories
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Get category by ID
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    // Get category by name
    public Optional<Category> getCategoryByName(String categoryName) {
        return categoryRepository.findByCategoryName(categoryName);
    }

    // Update category
    public Category updateCategory(
            Long id,
            Category updatedCategory) {

        Category existingCategory =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Category not found with ID: " + id));

        existingCategory.setCategoryName(
                updatedCategory.getCategoryName());

        existingCategory.setDescription(
                updatedCategory.getDescription());

        return categoryRepository.save(existingCategory);
    }

    // Delete category
    public void deleteCategory(Long id) {

        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException(
                    "Category not found with ID: " + id);
        }

        categoryRepository.deleteById(id);
    }
}