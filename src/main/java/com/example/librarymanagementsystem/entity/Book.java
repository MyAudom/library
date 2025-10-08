package com.example.librarymanagementsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private int publicationYear;
    private int totalCopies;
    private int availableCopies;

    @Column(name = "library_code", unique = true)
    private String libraryCode;

    @Column(name = "book_category")
    private String bookCategory;

    @Column(name = "created_date")
    private LocalDate createdDate;

    // Constructors
    public Book() {
        this.createdDate = LocalDate.now();
    }

    public Book(String title, String author, String isbn, int publicationYear, int totalCopies, int availableCopies, String libraryCode, String bookCategory) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.libraryCode = libraryCode;
        this.bookCategory = bookCategory;
        this.createdDate = LocalDate.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public int getPublicationYear() { return publicationYear; }
    public void setPublicationYear(int publicationYear) { this.publicationYear = publicationYear; }
    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }
    public String getLibraryCode() { return libraryCode; }
    public void setLibraryCode(String libraryCode) { this.libraryCode = libraryCode; }
    public String getBookCategory() { return bookCategory; }
    public void setBookCategory(String bookCategory) { this.bookCategory = bookCategory; }
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
}