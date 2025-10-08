package com.example.librarymanagementsystem.repository;

import com.example.librarymanagementsystem.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}