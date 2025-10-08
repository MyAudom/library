package com.example.librarymanagementsystem.controller;

import com.example.librarymanagementsystem.dto.CategoryStatsDto;
import com.example.librarymanagementsystem.service.CategoryStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryStatsController {

    @Autowired
    private CategoryStatsService categoryStatsService;

    @GetMapping("/top")
    public List<CategoryStatsDto> getTopCategories() {
        return categoryStatsService.getTopCategories();
    }
}