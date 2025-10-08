package com.example.librarymanagementsystem.repository;

import com.example.librarymanagementsystem.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    // Search by category
    List<Book> findByBookCategoryIgnoreCase(String bookCategory);

    // Search by title containing keyword
    List<Book> findByTitleContainingIgnoreCase(String title);

    // Search by author containing keyword
    List<Book> findByAuthorContainingIgnoreCase(String author);

    // Search by library code
    List<Book> findByLibraryCodeContainingIgnoreCase(String libraryCode);

    // Advanced search
    @Query("SELECT b FROM Book b WHERE " +
            "(:title IS NULL OR :title = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:author IS NULL OR :author = '' OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
            "(:category IS NULL OR :category = '' OR LOWER(b.bookCategory) = LOWER(:category)) AND " +
            "(:libraryCode IS NULL OR :libraryCode = '' OR LOWER(b.libraryCode) LIKE LOWER(CONCAT('%', :libraryCode, '%'))) AND " +
            "(:isbn IS NULL OR :isbn = '' OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :isbn, '%')))")
    List<Book> findBooksWithFilters(@Param("title") String title,
                                    @Param("author") String author,
                                    @Param("category") String category,
                                    @Param("libraryCode") String libraryCode,
                                    @Param("isbn") String isbn);

    // Find all distinct categories
    @Query("SELECT DISTINCT b.bookCategory FROM Book b WHERE b.bookCategory IS NOT NULL ORDER BY b.bookCategory")
    List<String> findAllDistinctCategories();

    // Check if library code exists
    boolean existsByLibraryCode(String libraryCode);
}