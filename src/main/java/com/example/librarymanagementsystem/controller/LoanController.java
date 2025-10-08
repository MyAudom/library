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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/loans")
public class LoanController {
    @Autowired
    private LoanService loanService;
    @Autowired
    private BookService bookService;
    @Autowired
    private MemberService memberService;

    @GetMapping
    public String listLoans(Model model) {
        model.addAttribute("loans", loanService.getAllLoans());
        return "loans";
    }

    @GetMapping("/new")
    public String showLoanForm(Model model) {
        model.addAttribute("loan", new Loan());
        model.addAttribute("books", bookService.getAllBooks());
        model.addAttribute("members", memberService.getAllMembers());
        return "loan-form";
    }

    @PostMapping
    public String saveLoan(@ModelAttribute Loan loan, @RequestParam Long bookId, @RequestParam Long memberId, Model model) {
        try {
            Book book = bookService.getBookById(bookId).orElse(null);
            Member member = memberService.getMemberById(memberId).orElse(null);
            if (book == null || member == null) {
                model.addAttribute("error", "Invalid book or member selected.");
                model.addAttribute("loan", loan);
                model.addAttribute("books", bookService.getAllBooks());
                model.addAttribute("members", memberService.getAllMembers());
                return "loan-form";
            }
            loan.setBook(book);
            loan.setMember(member);
            loan.setLoanDate(LocalDate.now());
            loan.setDueDate(LocalDate.now().plusWeeks(2));
            loanService.saveLoan(loan);
            return "redirect:/loans";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("loan", loan);
            model.addAttribute("books", bookService.getAllBooks());
            model.addAttribute("members", memberService.getAllMembers());
            return "loan-form";
        }
    }

    @GetMapping("/return/{id}")
    public String returnLoan(@PathVariable Long id, Model model) {
        try {
            Loan loan = loanService.getLoanById(id).orElseThrow(() -> new IllegalStateException("Loan not found."));
            loanService.returnLoan(id);
            return "redirect:/loans";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("loans", loanService.getAllLoans());
            return "loans";
        }
    }
}