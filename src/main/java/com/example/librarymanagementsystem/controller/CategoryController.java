package com.example.librarymanagementsystem.controller;

import com.example.librarymanagementsystem.entity.Category;
import com.example.librarymanagementsystem.service.BookService;
import com.example.librarymanagementsystem.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BookService bookService;

    @GetMapping
    public String listCategories(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Category> categories;

        if (search != null && !search.trim().isEmpty()) {
            categories = categoryService.searchCategories(search);
            model.addAttribute("searchKeyword", search);
        } else {
            categories = categoryService.getAllCategories();
        }

        model.addAttribute("categories", categories);
        return "category-list";
    }

    @GetMapping("/new")
    public String showCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("pageTitle", "Add New Category");
        return "category-form";
    }

    @PostMapping
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            if (category.getId() != null) {
                Category existingCategory = categoryService.getCategoryById(category.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid category Id: " + category.getId()));
                String oldName = existingCategory.getName();
                String newName = category.getName().trim();

                if (!oldName.equals(newName)) {
                    bookService.updateCategoryName(category.getId(), newName);
                } else {
                    categoryService.saveCategory(category);
                }
            } else {
                categoryService.saveCategory(category);
            }
            redirectAttributes.addFlashAttribute("success", "Category saved successfully!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/categories/" + (category.getId() == null ? "new" : "edit/" + category.getId());
        }
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.getCategoryById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category Id: " + id));
            model.addAttribute("category", category);
            model.addAttribute("pageTitle", "Edit Category");
            return "category-form";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/categories";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted successfully!");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categories";
    }

    @GetMapping("/api/all")
    @ResponseBody
    public List<Category> getAllCategoriesApi() {
        return categoryService.getAllActiveCategories();
    }

    @GetMapping("/api/search")
    @ResponseBody
    public List<Category> searchCategoriesApi(@RequestParam String term) {
        return categoryService.searchCategories(term);
    }

    // ADD THIS NEW ENDPOINT FOR TOP CATEGORIES
    @GetMapping("/api/top")
    @ResponseBody
    public List<Map<String, Object>> getTopCategories() {
        return categoryService.getTopCategoriesWithStats();
    }
}