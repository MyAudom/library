package com.example.librarymanagementsystem.service;

import com.example.librarymanagementsystem.entity.Member;
import com.example.librarymanagementsystem.repository.MemberRepository;
import com.example.librarymanagementsystem.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LoanRepository loanRepository;


    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Optional<Member> getMemberById(Long id) {
        return memberRepository.findById(id);
    }

    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }

    @Transactional
    public void deleteMember(Long id) {
        Optional<Member> memberOptional = memberRepository.findById(id);
        if (memberOptional.isEmpty()) {
            throw new IllegalStateException("Member not found.");
        }

        Member member = memberOptional.get();

        // Check for any loan history (active or returned)
        long totalLoansCount = loanRepository.countByMemberId(id);
        if (totalLoansCount > 0) {
            long activeLoansCount = loanRepository.countByMemberIdAndReturnDateIsNull(id);
            if (activeLoansCount > 0) {
                throw new IllegalStateException("Cannot delete member '" + member.getName() + "'. This member has " + activeLoansCount +
                        " active loan(s) that haven't been returned yet.");
            } else {
                long returnedLoansCount = loanRepository.countByMemberIdAndReturnDateIsNotNull(id);
                throw new IllegalStateException("Cannot delete member '" + member.getName() + "'. This member has loan history with " +
                        returnedLoansCount + " returned book(s). Data preservation required.");
            }
        }


        // Hard delete: remove the member from the database
        memberRepository.deleteById(id);
    }

    public long getTotalLoansForMember(Long memberId) {
        return loanRepository.countByMemberId(memberId);
    }

    public long getActiveLoansForMember(Long memberId) {
        return loanRepository.countByMemberIdAndReturnDateIsNull(memberId);
    }

    public long getReturnedLoansForMember(Long memberId) {
        return loanRepository.countByMemberIdAndReturnDateIsNotNull(memberId);
    }
}