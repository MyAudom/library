package com.example.librarymanagementsystem.controller;

import com.example.librarymanagementsystem.entity.Book;
import com.example.librarymanagementsystem.entity.Category;
import com.example.librarymanagementsystem.entity.Loan;
import com.example.librarymanagementsystem.entity.Member;
import com.example.librarymanagementsystem.service.BookService;
import com.example.librarymanagementsystem.service.CategoryService;
import com.example.librarymanagementsystem.service.LoanService;
import com.example.librarymanagementsystem.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/library")
public class ApiController {
    @Autowired
    private BookService bookService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private CategoryService categoryService;

    // Book Endpoints
    @GetMapping("/books")
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Optional<Book> book = bookService.getBookById(id);
        return book.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<String> updateBook(@PathVariable Long id, @RequestBody Book book) {
        try {
            if (!bookService.getBookById(id).isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found.");
            }
            book.setId(id);
            bookService.saveBook(book);
            return ResponseEntity.ok("Book updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.ok("Book deleted successfully.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Member Endpoints
    @GetMapping("/members")
    public List<Member> getAllMembers() {
        return memberService.getAllMembers();
    }

    @GetMapping("/members/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        Optional<Member> member = memberService.getMemberById(id);
        return member.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/members/{id}")
    public ResponseEntity<String> updateMember(@PathVariable Long id, @RequestBody Member member) {
        try {
            if (!memberService.getMemberById(id).isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found.");
            }
            member.setId(id);
            memberService.saveMember(member);
            return ResponseEntity.ok("Member updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/members/{id}")
    public ResponseEntity<String> deleteMember(@PathVariable Long id) {
        try {
            memberService.deleteMember(id);
            return ResponseEntity.ok("Member deleted successfully.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Loan Endpoints
    @GetMapping("/loans")
    public List<Loan> getAllLoans() {
        return loanService.getAllLoans();
    }

    @GetMapping("/loans/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
        Optional<Loan> loan = loanService.getLoanById(id);
        return loan.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/loans/{id}")
    public ResponseEntity<String> updateLoan(@PathVariable Long id, @RequestBody Loan loan) {
        try {
            if (!loanService.getLoanById(id).isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Loan not found.");
            }
            loan.setId(id);
            loanService.saveLoan(loan);
            return ResponseEntity.ok("Loan updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/loans/{id}")
    public ResponseEntity<String> deleteLoan(@PathVariable Long id) {
        try {
            loanService.deleteLoan(id);
            return ResponseEntity.ok("Loan deleted successfully.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Category Endpoints
    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);
        return category.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/categories")
    public ResponseEntity<String> createCategory(@RequestBody Category category) {
        try {
            categoryService.saveCategory(category);
            return ResponseEntity.status(HttpStatus.CREATED).body("Category created successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<String> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        try {
            if (!categoryService.getCategoryById(id).isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found.");
            }
            category.setId(id);
            bookService.updateCategoryName(id, category.getName()); // Cascade update to books
            return ResponseEntity.ok("Category updated successfully.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok("Category deleted successfully.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Loan Statistics
    @GetMapping("/active-loans-count")
    public long getActiveLoansCount() {
        return loanService.getActiveLoansCount();
    }

    @GetMapping("/done-active-loans-count")
    public long getDoneActiveLoansCount() {
        return loanService.getDoneActiveLoansCount();
    }
}