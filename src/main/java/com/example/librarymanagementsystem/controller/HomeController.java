package com.example.librarymanagementsystem.controller;

import com.example.librarymanagementsystem.entity.Book;
import com.example.librarymanagementsystem.entity.Loan;
import com.example.librarymanagementsystem.entity.Member;
import com.example.librarymanagementsystem.service.BookService;
import com.example.librarymanagementsystem.service.LoanService;
import com.example.librarymanagementsystem.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {
    @Autowired
    private BookService bookService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private LoanService loanService;

    @GetMapping("/")
    public String home(Model model) {
        // ទិន្នន័យដើម
        model.addAttribute("totalBooks", bookService.getAllBooks().size());
        model.addAttribute("totalMembers", memberService.getAllMembers().size());
        model.addAttribute("activeLoans", loanService.getActiveLoansCount());
        model.addAttribute("returnedLoans", loanService.getDoneActiveLoansCount());

        // សមាជិកដែលខ្ចីច្រើនជាងគេ (Top 5)
        List<Map<String, Object>> topBorrowers = getTopBorrowers();
        model.addAttribute("topBorrowers", topBorrowers);

        // សៀវភៅពេញនិយម (Top 5)
        List<Map<String, Object>> popularBooks = getPopularBooks();
        model.addAttribute("popularBooks", popularBooks);

        // សៀវភៅថ្មីៗ (Latest 5)
        List<Book> recentBooks = getRecentlyAddedBooks();
        model.addAttribute("recentBooks", recentBooks);

        // ការខ្ចីហួសកំណត់ពេល
        int overdueCount = getOverdueLoansCount();
        model.addAttribute("overdueLoansCount", overdueCount);

        return "index";
    }

    // API endpoint សម្រាប់ការជូនដំណឹងប្រភេទ SMS
    @GetMapping("/api/notifications")
    @ResponseBody
    public Map<String, Object> getNotifications() {
        Map<String, Object> notifications = new java.util.HashMap<>();

        int overdueCount = getOverdueLoansCount();
        if (overdueCount > 0) {
            notifications.put("type", "warning");
            notifications.put("message", "មានការខ្ចីសៀវភៅចំនួន " + overdueCount + " ដែលហួសកំណត់ពេល!");
            notifications.put("count", overdueCount);
        } else {
            notifications.put("type", "success");
            notifications.put("message", "មិនមានការខ្ចីហួសកំណត់ពេលទេ");
            notifications.put("count", 0);
        }

        return notifications;
    }

    private List<Map<String, Object>> getTopBorrowers() {
        List<Member> allMembers = memberService.getAllMembers();

        return allMembers.stream()
                .map(member -> {
                    Map<String, Object> borrowerInfo = new java.util.HashMap<>();
                    int loanCount = loanService.getLoanCountByMember(member.getId());
                    borrowerInfo.put("member", member);
                    borrowerInfo.put("loanCount", loanCount);
                    return borrowerInfo;
                })
                .sorted((a, b) -> Integer.compare((Integer) b.get("loanCount"), (Integer) a.get("loanCount")))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getPopularBooks() {
        List<Book> allBooks = bookService.getAllBooks();

        return allBooks.stream()
                .map(book -> {
                    Map<String, Object> bookInfo = new java.util.HashMap<>();
                    int borrowCount = loanService.getBorrowCountByBook(book.getId());
                    bookInfo.put("book", book);
                    bookInfo.put("borrowCount", borrowCount);
                    return bookInfo;
                })
                .sorted((a, b) -> Integer.compare((Integer) b.get("borrowCount"), (Integer) a.get("borrowCount")))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<Book> getRecentlyAddedBooks() {
        List<Book> allBooks = bookService.getAllBooks();

        return allBooks.stream()
                .sorted((a, b) -> {
                    // Assuming Book has a createdDate field
                    // If not available, you can sort by ID (newer books have higher IDs)
                    return Long.compare(b.getId(), a.getId());
                })
                .limit(5)
                .collect(Collectors.toList());
    }

    private int getOverdueLoansCount() {
        List<Loan> activeLoans = loanService.getActiveLoans();
        LocalDateTime now = LocalDateTime.now();

        return (int) activeLoans.stream()
                .filter(loan -> loan.getDueDate() != null && loan.getDueDate().isBefore(now.toLocalDate()))
                .count();
    }
}

// Note: You'll need to add these methods to your LoanService.java:
// - getLoanCountByMember(Long memberId) - use existing countByMemberId
// - getBorrowCountByBook(Long bookId) - use existing countByBookId