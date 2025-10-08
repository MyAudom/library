package com.example.librarymanagementsystem.repository;

import com.example.librarymanagementsystem.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    // រាប់ការខ្ចីដែលមិនទាន់ត្រលប់
    long countByReturnDateIsNull();

    // រាប់ការខ្ចីដែលត្រលប់រួចហើយ
    long countByReturnDateIsNotNull();

    // រាប់ការខ្ចីដែលមិនទាន់ត្រលប់សម្រាប់សៀវភៅជាក់លាក់មួយ
    long countByBookIdAndReturnDateIsNull(Long bookId);

    // រាប់ការខ្ចីដែលមិនទាន់ត្រលប់សម្រាប់សមាជិកជាក់លាក់មួយ
    long countByMemberIdAndReturnDateIsNull(Long memberId);

    // រាប់ការខ្ចីទាំងអស់ (active និង returned) សម្រាប់សៀវភៅជាក់លាក់មួយ
    long countByBookId(Long bookId);

    // រាប់ការខ្ចីទាំងអស់ (active និង returned) សម្រាប់សមាជិកជាក់លាក់មួយ
    long countByMemberId(Long memberId);

    // រាប់ការខ្ចីដែលត្រលប់រួចហើយសម្រាប់សៀវភៅជាក់លាក់មួយ
    long countByBookIdAndReturnDateIsNotNull(Long bookId);

    // រាប់ការខ្ចីដែលត្រលប់រួចហើយសម្រាប់សមាជិកជាក់លាក់មួយ
    long countByMemberIdAndReturnDateIsNotNull(Long memberId);

    // ស្វែងរកការខ្ចីដែលត្រលប់រួចហើយសម្រាប់ book
    List<Loan> findByBookIdAndReturnDateIsNotNull(Long bookId);

    // ស្វែងរកការខ្ចីដែលត្រលប់រួចហើយសម្រាប់ member
    List<Loan> findByMemberIdAndReturnDateIsNotNull(Long memberId);

    List<Loan> findByReturnDateIsNull();
}