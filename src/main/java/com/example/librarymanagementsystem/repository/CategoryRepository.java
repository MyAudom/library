package com.example.librarymanagementsystem.repository;

import com.example.librarymanagementsystem.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find category by name
    Optional<Category> findByName(String name);

    // Check if category exists by name
    boolean existsByName(String name);

    // Find all categories ordered by name
    List<Category> findAllByOrderByNameAsc();

    // Search categories by name (case-insensitive)
    List<Category> findByNameContainingIgnoreCase(String name);

    // Find all active categories ordered by name
    List<Category> findAllByActiveTrueOrderByNameAsc();
}