package com.example.librarymanagementsystem.service;

import com.example.librarymanagementsystem.entity.Book;
import com.example.librarymanagementsystem.entity.Category;
import com.example.librarymanagementsystem.repository.BookRepository;
import com.example.librarymanagementsystem.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private CategoryService categoryService;


    // Get all books
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // Get book by ID
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    // Delete a book
    @Transactional
    public void deleteBook(Long id) {
        Optional<Book> bookOptional = bookRepository.findById(id);
        if (bookOptional.isEmpty()) {
            throw new IllegalStateException("Book not found.");
        }

        Book book = bookOptional.get();

        // Check for any loan history (active or returned)
        long totalLoansCount = loanRepository.countByBookId(id);
        if (totalLoansCount > 0) {
            throw new IllegalStateException("Cannot delete book '" + book.getTitle() + "'. This book has loan history and cannot be deleted.");
        }


        // Hard delete: remove the book from the database
        bookRepository.deleteById(id);
    }

    // Search methods
    public List<Book> searchBooksByCategory(String category) {
        return bookRepository.findByBookCategoryIgnoreCase(category);
    }

    public List<Book> searchBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Book> searchBooksByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }

    public List<Book> searchBooksByLibraryCode(String libraryCode) {
        return bookRepository.findByLibraryCodeContainingIgnoreCase(libraryCode);
    }

    public List<Book> searchBooksWithFilters(String title, String author, String category, String libraryCode, String isbn) {
        return bookRepository.findBooksWithFilters(title, author, category, libraryCode, isbn);
    }

    // Get all active category objects
    public List<Category> getAllCategoryObjects() {
        return categoryService.getAllActiveCategories();
    }

    // Save or update a book
    @Transactional
    public void saveBook(Book book) {
        // Validation: Ensure availableCopies <= totalCopies
        if (book.getAvailableCopies() > book.getTotalCopies()) {
            throw new IllegalArgumentException("Available copies cannot exceed total copies.");
        }
        if (book.getTotalCopies() < 0 || book.getAvailableCopies() < 0) {
            throw new IllegalArgumentException("Copies cannot be negative.");
        }

        // Validate library code
        if (book.getLibraryCode() == null || book.getLibraryCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Library code is required.");
        }

        // Validate book category
        if (book.getBookCategory() == null || book.getBookCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Book category is required.");
        }

        // Check for duplicate library code
        if (book.getId() == null) {
            // New book
            if (bookRepository.existsByLibraryCode(book.getLibraryCode())) {
                throw new IllegalArgumentException("Library code '" + book.getLibraryCode() + "' already exists.");
            }
        } else {
            // Editing existing book
            Optional<Book> existingBook = bookRepository.findById(book.getId());
            if (existingBook.isPresent() &&
                    !existingBook.get().getLibraryCode().equals(book.getLibraryCode()) &&
                    bookRepository.existsByLibraryCode(book.getLibraryCode())) {
                throw new IllegalArgumentException("Library code '" + book.getLibraryCode() + "' already exists.");
            }
        }

        // Ensure category exists or create it (only active categories)
        Category category = categoryService.findOrCreateCategory(book.getBookCategory().trim());
        if (!category.isActive()) {
            throw new IllegalArgumentException("Cannot assign book to inactive category '" + category.getName() + "'.");
        }
        book.setBookCategory(category.getName());

        bookRepository.save(book);
    }

    // Get all active category names
    public List<String> getAllCategories() {
        return categoryService.getAllActiveCategories()
                .stream()
                .map(Category::getName)
                .collect(Collectors.toList());
    }

    // Update category name and cascade to books
    @Transactional
    public void updateCategoryName(Long categoryId, String newName) {
        Optional<Category> categoryOptional = categoryService.getCategoryById(categoryId);
        if (categoryOptional.isEmpty()) {
            throw new IllegalStateException("Category not found.");
        }

        Category category = categoryOptional.get();
        String oldName = category.getName();

        // Update category name
        category.setName(newName.trim());
        categoryService.saveCategory(category);

        // Update all books with the old category name
        List<Book> books = bookRepository.findByBookCategoryIgnoreCase(oldName);
        for (Book book : books) {
            book.setBookCategory(newName.trim());
            bookRepository.save(book);
        }
    }
}