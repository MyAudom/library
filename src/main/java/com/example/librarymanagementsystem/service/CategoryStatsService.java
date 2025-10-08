package com.example.librarymanagementsystem.service;

import com.example.librarymanagementsystem.dto.CategoryStatsDto;
import com.example.librarymanagementsystem.entity.Category;
import com.example.librarymanagementsystem.entity.Book;
import com.example.librarymanagementsystem.repository.BookRepository;
import com.example.librarymanagementsystem.repository.CategoryRepository;
import com.example.librarymanagementsystem.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class CategoryStatsService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    public List<CategoryStatsDto> getTopCategories() {
        // Get all active categories
        List<Category> categories = categoryRepository.findAllByActiveTrueOrderByNameAsc();

        // Convert to DTOs with statistics
        List<CategoryStatsDto> categoryStats = categories.stream()
                .map(this::createCategoryStats)
                .collect(Collectors.toList());

        // Sort by total activity (books + loans + returns) descending
        categoryStats.sort((a, b) -> {
            long totalA = a.getBooksCount() + a.getLoansCount() + a.getReturnsCount();
            long totalB = b.getBooksCount() + b.getLoansCount() + b.getReturnsCount();
            return Long.compare(totalB, totalA);
        });

        // Return top 5 categories
        return categoryStats.stream()
                .limit(5)
                .collect(Collectors.toList());
    }

    private CategoryStatsDto createCategoryStats(Category category) {
        String categoryName = category.getName();

        // Count books in this category
        long booksCount = bookRepository.findByBookCategoryIgnoreCase(categoryName).size();

        // Count total loans for books in this category (active + returned)
        long totalLoans = bookRepository.findByBookCategoryIgnoreCase(categoryName)
                .stream()
                .mapToLong(book -> loanRepository.countByBookId(book.getId()))
                .sum();

        // Count returned loans for books in this category
        long returnedLoans = bookRepository.findByBookCategoryIgnoreCase(categoryName)
                .stream()
                .mapToLong(book -> loanRepository.countByBookIdAndReturnDateIsNotNull(book.getId()))
                .sum();

        return new CategoryStatsDto(categoryName, booksCount, totalLoans, returnedLoans);
    }
}