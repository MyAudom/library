package com.example.librarymanagementsystem.service;

import com.example.librarymanagementsystem.entity.Book;
import com.example.librarymanagementsystem.entity.Loan;
import com.example.librarymanagementsystem.repository.BookRepository;
import com.example.librarymanagementsystem.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private BookRepository bookRepository;


    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public Optional<Loan> getLoanById(Long id) {
        return loanRepository.findById(id);
    }

    public Loan saveLoan(Loan loan) {
        // Validate available copies
        Book book = loan.getBook();
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No available copies of the book to loan.");
        }

        // Decrease available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // Save the loan
        return loanRepository.save(loan);
    }

    public void returnLoan(Long loanId) {
        Optional<Loan> loanOptional = loanRepository.findById(loanId);
        if (loanOptional.isPresent()) {
            Loan loan = loanOptional.get();
            if (loan.getReturnDate() == null) {
                // Set return date
                loan.setReturnDate(java.time.LocalDate.now());

                // Increase available copies
                Book book = loan.getBook();
                book.setAvailableCopies(book.getAvailableCopies() + 1);
                bookRepository.save(book);

                // Save the loan
                loanRepository.save(loan);
            } else {
                throw new IllegalStateException("Loan has already been returned.");
            }
        } else {
            throw new IllegalStateException("Loan not found.");
        }
    }

    public void deleteLoan(Long id) {
        loanRepository.deleteById(id);
    }

    public long getActiveLoansCount() {
        return loanRepository.countByReturnDateIsNull();
    }

    public long getDoneActiveLoansCount() {
        return loanRepository.countByReturnDateIsNotNull();
    }

    // បន្ថែម method ថ្មីសម្រាប់លុប loans ដែលត្រលប់រួចសម្រាប់ book ជាក់លាក់
    @Transactional
    public void deleteReturnedLoansByBookId(Long bookId) {
        List<Loan> returnedLoans = loanRepository.findByBookIdAndReturnDateIsNotNull(bookId);
        loanRepository.deleteAll(returnedLoans);
    }

    // បន្ថែម method ថ្មីសម្រាប់លុប loans ដែលត្រលប់រួចសម្រាប់ member ជាក់លាក់
    @Transactional
    public void deleteReturnedLoansByMemberId(Long memberId) {
        List<Loan> returnedLoans = loanRepository.findByMemberIdAndReturnDateIsNotNull(memberId);
        loanRepository.deleteAll(returnedLoans);
    }

    // ពិនិត្យថាតើមាន active loans ដែរទេ
    public boolean hasActiveLoansForBook(Long bookId) {
        return loanRepository.countByBookIdAndReturnDateIsNull(bookId) > 0;
    }

    public boolean hasActiveLoansForMember(Long memberId) {
        return loanRepository.countByMemberIdAndReturnDateIsNull(memberId) > 0;
    }

    // ===== Methods ថ្មីសម្រាប់ Dashboard =====

    /**
     * រកចំនួនការខ្ចីតាមសមាជិក (សម្រាប់ Top Borrowers)
     */
    public int getLoanCountByMember(Long memberId) {
        return (int) loanRepository.countByMemberId(memberId);
    }

    /**
     * រកចំនួនការខ្ចីតាមសៀវភៅ (សម្រាប់ Popular Books)
     */
    public int getBorrowCountByBook(Long bookId) {
        return (int) loanRepository.countByBookId(bookId);
    }

    /**
     * រកចំនួនការខ្ចីដែលហួសកំណត់ពេល
     */
    public int getOverdueLoansCount() {
        LocalDate today = LocalDate.now();
        List<Loan> allLoans = getAllLoans();

        return (int) allLoans.stream()
                .filter(loan -> loan.getReturnDate() == null &&
                        loan.getDueDate() != null &&
                        loan.getDueDate().isBefore(today))
                .count();
    }

    /**
     * រកការខ្ចីដែលហួសកំណត់ពេល
     */
    public List<Loan> getOverdueLoans() {
        LocalDate today = LocalDate.now();
        List<Loan> allLoans = getAllLoans();

        return allLoans.stream()
                .filter(loan -> loan.getReturnDate() == null &&
                        loan.getDueDate() != null &&
                        loan.getDueDate().isBefore(today))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Loan> getActiveLoans() {
        return loanRepository.findByReturnDateIsNull();
    }
}