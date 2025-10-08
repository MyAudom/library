package com.example.librarymanagementsystem.service;

import com.example.librarymanagementsystem.entity.Book;
import com.example.librarymanagementsystem.entity.Category;
import com.example.librarymanagementsystem.repository.BookRepository;
import com.example.librarymanagementsystem.repository.CategoryRepository;
import com.example.librarymanagementsystem.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;

    // Get all categories (including inactive for admin views)
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    // Get all active categories
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findAllByActiveTrueOrderByNameAsc();
    }

    // Get category by ID
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    // Get category by name
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    // Save a new category
    @Transactional
    public Category saveNewCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }

        String trimmedName = categoryName.trim();

        if (categoryRepository.existsByName(trimmedName)) {
            return categoryRepository.findByName(trimmedName)
                    .orElseThrow(() -> new RuntimeException("Unexpected error finding category"));
        }

        Category newCategory = new Category(trimmedName);
        return categoryRepository.save(newCategory);
    }

    // Find or create a category
    @Transactional
    public Category findOrCreateCategory(String categoryName) {
        String trimmedName = categoryName.trim();
        Optional<Category> existingCategory = categoryRepository.findByName(trimmedName);
        if (existingCategory.isPresent()) {
            return existingCategory.get();
        }
        return saveNewCategory(trimmedName);
    }

    // Check if category exists
    public boolean categoryExists(String name) {
        return categoryRepository.existsByName(name);
    }

    // Search categories
    public List<Category> searchCategories(String search) {
        return categoryRepository.findByNameContainingIgnoreCase(search.trim());
    }

    // Save or update a category
    @Transactional
    public void saveCategory(Category category) {
        if (category == null || category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        String trimmedName = category.getName().trim();

        // Check for duplicate name (excluding the current category)
        Optional<Category> existingCategory = categoryRepository.findByName(trimmedName);
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(category.getId())) {
            throw new IllegalArgumentException("Category name '" + trimmedName + "' already exists.");
        }

        category.setName(trimmedName);
        categoryRepository.save(category);
    }

    // Soft delete a category
    @Transactional
    public void deleteCategory(Long id) {
        Optional<Category> categoryOptional = categoryRepository.findById(id);
        if (categoryOptional.isEmpty()) {
            throw new IllegalStateException("Category not found.");
        }

        Category category = categoryOptional.get();

        // Check if the category is associated with any books
        List<Book> books = bookRepository.findByBookCategoryIgnoreCase(category.getName());
        if (!books.isEmpty()) {
            throw new IllegalStateException("Cannot delete category '" + category.getName() + "'. It is associated with " + books.size() + " book(s).");
        }

        // Soft delete: set active to false
        category.setActive(false);
        categoryRepository.save(category);
    }
    // ADD THIS METHOD TO YOUR EXISTING CategoryService.java

    @Autowired
    private LoanRepository loanRepository;

    // Add this method to your existing CategoryService class
    public List<Map<String, Object>> getTopCategoriesWithStats() {
        List<Category> activeCategories = getAllActiveCategories();

        List<Map<String, Object>> categoryStats = activeCategories.stream()
                .map(category -> {
                    Map<String, Object> stats = new HashMap<>();
                    String categoryName = category.getName();

                    // Get books in this category
                    List<Book> books = bookRepository.findByBookCategoryIgnoreCase(categoryName);
                    long booksCount = books.size();

                    // Calculate total loans for books in this category
                    long totalLoans = books.stream()
                            .mapToLong(book -> loanRepository.countByBookId(book.getId()))
                            .sum();

                    // Calculate returned loans for books in this category
                    long returnedLoans = books.stream()
                            .mapToLong(book -> loanRepository.countByBookIdAndReturnDateIsNotNull(book.getId()))
                            .sum();

                    stats.put("name", categoryName);
                    stats.put("booksCount", booksCount);
                    stats.put("loansCount", totalLoans);
                    stats.put("returnsCount", returnedLoans);

                    return stats;
                })
                .sorted((a, b) -> {
                    // Sort by total activity (books + loans + returns) descending
                    long totalA = (Long)a.get("booksCount") + (Long)a.get("loansCount") + (Long)a.get("returnsCount");
                    long totalB = (Long)b.get("booksCount") + (Long)b.get("loansCount") + (Long)b.get("returnsCount");
                    return Long.compare(totalB, totalA);
                })
                .limit(5)
                .collect(Collectors.toList());

        return categoryStats;
    }
}