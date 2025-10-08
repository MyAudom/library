package com.example.librarymanagementsystem.controller;

import com.example.librarymanagementsystem.entity.Book;
import com.example.librarymanagementsystem.entity.Category;
import com.example.librarymanagementsystem.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/books")
public class BookController {
    @Autowired
    private BookService bookService;

    // List all books with optional search and filter
    @GetMapping
    public String listBooks(@RequestParam(value = "search", required = false) String search,
                            @RequestParam(value = "category", required = false) String category,
                            @RequestParam(value = "title", required = false) String title,
                            @RequestParam(value = "author", required = false) String author,
                            @RequestParam(value = "libraryCode", required = false) String libraryCode,
                            @RequestParam(value = "isbn", required = false) String isbn,
                            Model model) {

        List<Book> books;

        // Advanced search with filters
        if ((title != null && !title.trim().isEmpty()) ||
                (author != null && !author.trim().isEmpty()) ||
                (category != null && !category.trim().isEmpty()) ||
                (libraryCode != null && !libraryCode.trim().isEmpty()) ||
                (isbn != null && !isbn.trim().isEmpty())) {
            books = bookService.searchBooksWithFilters(title, author, category, libraryCode, isbn);
            model.addAttribute("searchPerformed", true);
            model.addAttribute("searchFilters", true);
        }
        // Simple search by category
        else if (category != null && !category.trim().isEmpty()) {
            books = bookService.searchBooksByCategory(category);
            model.addAttribute("searchPerformed", true);
            model.addAttribute("selectedCategory", category);
        }
        // General search (search in title, author, library code)
        else if (search != null && !search.trim().isEmpty()) {
            books = bookService.searchBooksWithFilters(search, search, null, search, search);
            model.addAttribute("searchPerformed", true);
            model.addAttribute("searchKeyword", search);
        }
        // Show all books
        else {
            books = bookService.getAllBooks();
        }

        model.addAttribute("books", books);
        model.addAttribute("categories", bookService.getAllCategoryObjects()); // Only active categories
        model.addAttribute("searchTitle", title);
        model.addAttribute("searchAuthor", author);
        model.addAttribute("searchCategory", category);
        model.addAttribute("searchLibraryCode", libraryCode);
        model.addAttribute("searchIsbn", isbn);
        model.addAttribute("searchKeyword", search);

        return "book-list";
    }

    // Show form for creating a new book
    @GetMapping("/new")
    public String showBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", bookService.getAllCategoryObjects()); // Only active categories
        model.addAttribute("pageTitle", "Add New Book");
        return "book-form";
    }

    // Save a book (create or update)
    @PostMapping
    public String saveBook(@ModelAttribute Book book, RedirectAttributes redirectAttributes) {
        try {
            bookService.saveBook(book);
            redirectAttributes.addFlashAttribute("success", "Book saved successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/books/" + (book.getId() == null ? "new" : "edit/" + book.getId());
        }
        return "redirect:/books";
    }

    // Show form for editing an existing book
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Book book = bookService.getBookById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid book ID: " + id));
            model.addAttribute("book", book);
            model.addAttribute("categories", bookService.getAllCategoryObjects()); // Only active categories
            model.addAttribute("pageTitle", "Edit Book");
            return "book-form";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/books";
        }
    }

    // Delete a book
    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Book> bookOptional = bookService.getBookById(id);
            if (bookOptional.isEmpty()) {
                throw new IllegalStateException("Book not found.");
            }
            Book book = bookOptional.get();
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("success", "Book deleted successfully!");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/books";
    }

    // API endpoint for getting books by category (for AJAX calls)
    @GetMapping("/api/category/{category}")
    @ResponseBody
    public List<Book> getBooksByCategory(@PathVariable String category) {
        return bookService.searchBooksByCategory(category);
    }

    // API endpoint for getting all categories
    @GetMapping("/api/categories")
    @ResponseBody
    public List<String> getAllCategories() {
        return bookService.getAllCategories();
    }

    // API endpoint for getting all active category objects
    @GetMapping("/api/category-objects")
    @ResponseBody
    public List<Category> getAllCategoryObjects() {
        return bookService.getAllCategoryObjects();
    }
    @GetMapping("/api/categories/top")
    @ResponseBody
    public List<Map<String, Object>> getTopCategories() {
        List<String> categories = bookService.getAllCategories();

        return categories.stream()
                .limit(5)
                .map(categoryName -> {
                    Map<String, Object> stats = new HashMap<>();

                    // Count books in this category
                    List<Book> booksInCategory = bookService.searchBooksByCategory(categoryName);
                    long booksCount = booksInCategory.size();

                    // For now, let's use simple counts (you can enhance later)
                    long loansCount = booksInCategory.stream()
                            .mapToLong(book -> book.getTotalCopies() - book.getAvailableCopies())
                            .sum();

                    // Placeholder for returns (you can calculate actual returns later)
                    long returnsCount = loansCount / 2; // Simple estimate

                    stats.put("name", categoryName);
                    stats.put("booksCount", booksCount);
                    stats.put("loansCount", loansCount);
                    stats.put("returnsCount", returnsCount);

                    return stats;
                })
                .sorted((a, b) -> {
                    long totalA = (Long)a.get("booksCount") + (Long)a.get("loansCount");
                    long totalB = (Long)b.get("booksCount") + (Long)b.get("loansCount");
                    return Long.compare(totalB, totalA);
                })
                .collect(Collectors.toList());
    }
    // API endpoint for validating duplicate title
    @GetMapping("/api/validate-title")
    @ResponseBody
    public boolean validateTitle(@RequestParam String value, @RequestParam(required = false) String currentId) {
        List<Book> books = bookService.getAllBooks();
        Long currentBookId = currentId != null && !currentId.isEmpty() ? Long.parseLong(currentId) : null;

        return books.stream()
                .filter(book -> !book.getId().equals(currentBookId)) // Exclude current book in edit mode
                .anyMatch(book -> book.getTitle().equalsIgnoreCase(value.trim()));
    }

    // API endpoint for validating duplicate ISBN
    @GetMapping("/api/validate-isbn")
    @ResponseBody
    public boolean validateIsbn(@RequestParam String value, @RequestParam(required = false) String currentId) {
        List<Book> books = bookService.getAllBooks();
        Long currentBookId = currentId != null && !currentId.isEmpty() ? Long.parseLong(currentId) : null;

        return books.stream()
                .filter(book -> !book.getId().equals(currentBookId)) // Exclude current book in edit mode
                .anyMatch(book -> book.getIsbn().equalsIgnoreCase(value.trim()));
    }

    // API endpoint for validating duplicate library code
    @GetMapping("/api/validate-libraryCode")
    @ResponseBody
    public boolean validateLibraryCode(@RequestParam String value, @RequestParam(required = false) String currentId) {
        List<Book> books = bookService.getAllBooks();
        Long currentBookId = currentId != null && !currentId.isEmpty() ? Long.parseLong(currentId) : null;

        return books.stream()
                .filter(book -> !book.getId().equals(currentBookId)) // Exclude current book in edit mode
                .anyMatch(book -> book.getLibraryCode().equalsIgnoreCase(value.trim()));
    }
}