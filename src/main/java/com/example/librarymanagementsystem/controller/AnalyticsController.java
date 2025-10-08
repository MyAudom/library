package com.example.librarymanagementsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnalyticsController {
    @GetMapping("/analytics")
    public String showDashboard() {
        return "analytics"; // Renders src/main/resources/templates/dashboard.html
    }
}