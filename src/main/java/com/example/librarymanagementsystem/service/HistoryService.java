package com.example.librarymanagementsystem.service;

import com.example.librarymanagementsystem.repository.BookRepository;
import com.example.librarymanagementsystem.repository.LoanRepository;
import com.example.librarymanagementsystem.repository.MemberRepository;
import com.example.librarymanagementsystem.entity.Book;
import com.example.librarymanagementsystem.entity.Member;
import com.example.librarymanagementsystem.entity.Loan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HistoryService {
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private LoanRepository loanRepository;

    public List<DailyActivity> getWeeklyActivity() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6); // Changed to 6 to get exactly 7 days including today
        List<DailyActivity> activities = new ArrayList<>();

        // Initialize all days with zero values
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DailyActivity activity = new DailyActivity();
            activity.setDate(date.toString());
            activity.setTotalBooks(0);
            activity.setTotalMembers(0);
            activity.setActiveLoans(0);
            activity.setReturnedLoans(0);
            activity.setBooksAdded(0);
            activity.setBooksDeleted(0);
            activity.setMembersAdded(0);
            activity.setMembersDeleted(0);
            activity.setNewLoans(0);
            activity.setReturnedLoansDaily(0);
            activities.add(activity);
        }

        // Create a map for easy access by date
        Map<String, DailyActivity> activityMap = activities.stream()
                .collect(Collectors.toMap(DailyActivity::getDate, a -> a));

        // Count books by actual creation dates
        List<Book> books = bookRepository.findAll();
        books.forEach(book -> {
            if (book.getCreatedDate() != null) {
                String createdDateStr = book.getCreatedDate().toString();
                if (activityMap.containsKey(createdDateStr)) {
                    activityMap.get(createdDateStr).incrementBooksAdded();
                }
            }
        });

        // Count members by actual registration dates
        List<Member> members = memberRepository.findAll();
        members.forEach(member -> {
            if (member.getRegisteredDate() != null) {
                String registeredDateStr = member.getRegisteredDate().toString();
                if (activityMap.containsKey(registeredDateStr)) {
                    activityMap.get(registeredDateStr).incrementMembersAdded();
                }
            }
        });

        // Count loans and returns using existing date fields
        List<Loan> loans = loanRepository.findAll();
        loans.forEach(loan -> {
            // Count new loans by loan date
            if (loan.getLoanDate() != null) {
                String loanDateStr = loan.getLoanDate().toString();
                if (activityMap.containsKey(loanDateStr)) {
                    activityMap.get(loanDateStr).incrementNewLoans();
                }
            }

            // Count returned loans by return date
            if (loan.getReturnDate() != null) {
                String returnDateStr = loan.getReturnDate().toString();
                if (activityMap.containsKey(returnDateStr)) {
                    activityMap.get(returnDateStr).incrementReturnedLoansDaily();
                }
            }
        });

        // Calculate net active loans and set final values
        for (DailyActivity activity : activities) {
            // Show daily book additions (net change for that day)
            activity.setTotalBooks(activity.getBooksAdded());

            // Show daily member additions (net change for that day)
            activity.setTotalMembers(activity.getMembersAdded());

            // Calculate net active loans: new loans minus returned loans
            int netActiveLoans = activity.getNewLoans() - activity.getReturnedLoansDaily();
            activity.setActiveLoans(Math.max(0, netActiveLoans)); // Ensure it doesn't go negative

            // Show daily returned loans
            activity.setReturnedLoans(activity.getReturnedLoansDaily());
        }

        return activities;
    }

    // Inner class to hold daily activity data
    public static class DailyActivity {
        private String date;
        private int totalBooks; // Daily book additions only
        private int totalMembers; // Daily member additions only
        private int activeLoans; // Net active loans (new loans - returned loans)
        private int returnedLoans; // Daily returned loans only

        // Detailed tracking fields
        private int booksAdded;
        private int booksDeleted;
        private int membersAdded;
        private int membersDeleted;
        private int newLoans;
        private int returnedLoansDaily;

        // Getters and setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public int getTotalBooks() { return totalBooks; }
        public void setTotalBooks(int totalBooks) { this.totalBooks = totalBooks; }
        public int getTotalMembers() { return totalMembers; }
        public void setTotalMembers(int totalMembers) { this.totalMembers = totalMembers; }
        public int getActiveLoans() { return activeLoans; }
        public void setActiveLoans(int activeLoans) { this.activeLoans = activeLoans; }
        public int getReturnedLoans() { return returnedLoans; }
        public void setReturnedLoans(int returnedLoans) { this.returnedLoans = returnedLoans; }

        public int getBooksAdded() { return booksAdded; }
        public void setBooksAdded(int booksAdded) { this.booksAdded = booksAdded; }
        public int getBooksDeleted() { return booksDeleted; }
        public void setBooksDeleted(int booksDeleted) { this.booksDeleted = booksDeleted; }
        public int getMembersAdded() { return membersAdded; }
        public void setMembersAdded(int membersAdded) { this.membersAdded = membersAdded; }
        public int getMembersDeleted() { return membersDeleted; }
        public void setMembersDeleted(int membersDeleted) { this.membersDeleted = membersDeleted; }
        public int getNewLoans() { return newLoans; }
        public void setNewLoans(int newLoans) { this.newLoans = newLoans; }
        public int getReturnedLoansDaily() { return returnedLoansDaily; }
        public void setReturnedLoansDaily(int returnedLoansDaily) { this.returnedLoansDaily = returnedLoansDaily; }

        // Increment methods
        public void incrementBooksAdded() { this.booksAdded++; }
        public void incrementBooksDeleted() { this.booksDeleted++; }
        public void incrementMembersAdded() { this.membersAdded++; }
        public void incrementMembersDeleted() { this.membersDeleted++; }
        public void incrementNewLoans() { this.newLoans++; }
        public void incrementReturnedLoansDaily() { this.returnedLoansDaily++; }
    }
}